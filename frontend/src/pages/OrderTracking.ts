import type {Order, OrderStatus} from "../domain/order.ts";
import {createPayment, getOrder, verifiyPayment} from "../infrastructure/orderRest.ts";
import {getCurrentOrder, setCurrentOrder, subscribeOrder} from "../service/orderServices.ts";

export default function OrderTrackingPage(params?: Record<string, string>): HTMLElement {
    const el = document.createElement("div");
    el.className = "max-w-3xl mx-auto mt-8 px-4 fade-in";

    const orderId = params?.id;
    if (!orderId) {
        el.innerHTML = `<p class="text-red-500 text-center mt-10">No order ID provided.</p>`;
        return el;
    }

    const contentEl = document.createElement("div");
    el.appendChild(contentEl);

    const statusColors: Record<OrderStatus, string> = {
        "CART": "bg-blue-500",
        "CUSTOMER_DETAILS_PROVIDED": "bg-blue-500",
        "PAYMENT_IN_PROGRESS": "bg-blue-500",
        "PLACED": "bg-green-500",
        "ACCEPTED": "bg-green-500",
        "READY": "bg-green-500",
        "PICKED_UP": "bg-green-500",
        "DELIVERED": "bg-green-500",
        "CANCELLED": "bg-red-500",
        "REJECTED": "bg-red-500"
    };

    let progressBarDiv: HTMLDivElement;
    let progressInnerPulseDiv: HTMLDivElement;

    function createProgressBar(colorClass: string) {
        const wrapper = document.createElement("div");
        wrapper.className = "w-full bg-gray-200 rounded-full h-4 mb-4 overflow-hidden shadow-inner relative";

        progressBarDiv = document.createElement("div");
        progressBarDiv.className = `${colorClass} h-4 transition-all duration-1000 ease-in-out`;
        progressBarDiv.style.width = "0%";

        progressInnerPulseDiv = document.createElement("div");
        progressInnerPulseDiv.className = `absolute top-0 left-0 w-full h-full ${colorClass.replace('500', '400')} opacity-25 animate-pulse`;

        progressBarDiv.appendChild(progressInnerPulseDiv);
        wrapper.appendChild(progressBarDiv);

        return wrapper;
    }

    async function render(order: Order) {
        const colorClass = statusColors[order.status] || "bg-gray-500";

        // Als content nog leeg is, bouw eerste keer op
        if (!contentEl.hasChildNodes()) {
            contentEl.innerHTML = `
                <h1 class="text-3xl font-bold mb-6 text-center">📦 Order Tracking</h1>
                <p class="text-gray-600 mb-4 text-center">Order ID: <span class="font-semibold">${order.id}</span></p>
            `;
            contentEl.appendChild(createProgressBar(colorClass));

            const statusText = document.createElement("p");
            statusText.className = "text-center font-medium mb-6";
            statusText.innerHTML = `Status: <span class="capitalize">${order.status.toLowerCase().replace(/_/g, " ")}</span>`;
            contentEl.appendChild(statusText);

            const linesUl = document.createElement("ul");
            linesUl.className = "space-y-2 mb-6";
            contentEl.appendChild(linesUl);

            const totalP = document.createElement("p");
            totalP.className = "text-right font-semibold text-lg";
            contentEl.appendChild(totalP);

            const actionDiv = document.createElement("div");
            actionDiv.id = "order-actions";
            actionDiv.className = "flex justify-center gap-4 mt-6";
            contentEl.appendChild(actionDiv);
        }

        // Update progress-bar breedte smooth
        const widthMap: Record<OrderStatus, string> = {
            "CART": "0%",
            "CUSTOMER_DETAILS_PROVIDED": "5%",
            "PAYMENT_IN_PROGRESS": "10%",
            "PLACED": "25%",
            "ACCEPTED": "50%",
            "READY": "75%",
            "PICKED_UP": "91.6%",
            "DELIVERED": "100%",
            "CANCELLED": "0%",
            "REJECTED": "0%"
        };
        progressBarDiv.style.width = widthMap[order.status] || "0%";

        // Pulse-effect
        progressInnerPulseDiv.classList.add("animate-pulse");
        setTimeout(() => progressInnerPulseDiv.classList.remove("animate-pulse"), 400);

        // Update status text
        const statusText = contentEl.querySelector("p.text-center.font-medium")!;
        statusText.innerHTML = `Status: <span class="capitalize">${order.status.toLowerCase().replace(/_/g, " ")}</span>`;

        // Update order lines
        const linesUl = contentEl.querySelector("ul")!;
        linesUl.innerHTML = order.lines.map(l => `
            <li class="flex justify-between border p-3 rounded hover:shadow-md transition">
                <span>${l.dishName}</span>
                <span class="font-medium">€${l.totalPrice.toFixed(2)}</span>
            </li>
        `).join("");

        // Update totaalprijs
        const totalP = contentEl.querySelector("p.text-right")!;
        totalP.textContent = `Total: €${order.totalPrice.toFixed(2)}`;

        const actionDiv = contentEl.querySelector<HTMLDivElement>("#order-actions")!;
        actionDiv.innerHTML = "";

        if (order.status === "CUSTOMER_DETAILS_PROVIDED") {
            const btn = document.createElement("button");
            btn.textContent = "Create Payment";
            btn.className = "bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded font-semibold";
            btn.onclick = async () => {
                try {
                    const paymentOrder = await createPayment(order.id);
                    setCurrentOrder(paymentOrder);
                    render(paymentOrder);
                } catch (err: any) {
                    alert(err?.message || "Failed to create payment.");
                }
            };
            actionDiv.appendChild(btn);
        }

        if (order.status === "PAYMENT_IN_PROGRESS") {
            const payBtn = document.createElement("button");
            payBtn.textContent = "Go to Payment Page";
            payBtn.className = "bg-green-600 hover:bg-green-700 text-white px-5 py-2 rounded font-semibold";
            payBtn.onclick = () => {
                if (order.paymentUrl) {
                    window.location.href = order.paymentUrl;
                } else {
                    alert("No payment URL available.");
                }
            };

            const verifyBtn = document.createElement("button");
            verifyBtn.textContent = "Verify Payment";
            verifyBtn.className = "bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded font-semibold";
            verifyBtn.onclick = async () => {
                try {
                    await verifiyPayment(order.id);
                    const latest = await getOrder(order.id);
                    setCurrentOrder(latest);
                    render(latest);
                } catch (err: any) {
                    alert(err?.message || "Failed to verify payment.");
                }
            };

            actionDiv.append(payBtn, verifyBtn);
        }
    }

    (async () => {
        try {
            const order = await getOrder(orderId);
            setCurrentOrder(order);
            render(order);
        } catch {
            contentEl.innerHTML = `<p class="text-red-500 text-center mt-10">Failed to load order.</p>`;
        }
    })();

    const interval = setInterval(async () => {
        try {
            const latest = await getOrder(orderId);
            const current = getCurrentOrder();
            if (!current || latest.status !== current.status) {
                setCurrentOrder(latest);
                render(latest);

                if (["DELIVERED", "CANCELLED", "REJECTED"].includes(latest.status)) {
                    clearInterval(interval);
                }
            }
        } catch {
            console.warn("Auto-refresh failed.");
        }
    }, 10000);

    window.addEventListener("beforeunload", () => clearInterval(interval));

    subscribeOrder((order) => {
        if (order?.id === orderId) render(order);
    });

    return el;
}

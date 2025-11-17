import type {Order} from "../domain/order.ts";
import {getOrder, setCustomerDetails, type PlaceOrderDto, createPayment} from "../infrastructure/orderRest.ts";
import {resetCurrentOrder, validateOrderDto} from "../service/orderServices.ts";

export default function OrderCheckoutPage(params?: Record<string, string>): HTMLElement {
    const el = document.createElement("div");
    const orderId = params?.id;
    el.className = "max-w-3xl mx-auto mt-8 px-4 fade-in";

    if (!orderId) {
        el.innerHTML = `<p class="text-red-500 text-center mt-10">No order ID provided.</p>`;
        return el;
    }

    (async () => {
        try {
            const order = await getOrder(orderId);
            if (!order) return showError("Order not found.");
            renderCart(order);
        } catch {
            showError("Failed to load order.");
        }
    })();

    function showError(msg: string) {
        el.innerHTML = `<p class="text-red-500 text-center mt-10">${msg}</p>`;
    }

    function renderCart(order: Order) {
        el.innerHTML = `
            <h1 class="text-3xl font-bold mb-6 text-center">🛒 Your Cart</h1>
            <ul class="space-y-4 mb-6">
                ${order.lines.map(line => `
                    <li class="border p-4 rounded flex justify-between items-center hover:shadow-md transition">
                        <div>
                            <p class="font-semibold text-lg">${line.dishName}</p>
                            <p class="text-gray-600 text-sm">Qty: ${line.quantity}</p>
                            ${line.notes ? `<p class="text-gray-500 text-sm">${line.notes}</p>` : ""}
                        </div>
                        <p class="font-medium text-lg">€${line.totalPrice.toFixed(2)}</p>
                    </li>
                `).join("")}
            </ul>
            <p class="text-right font-semibold text-xl mb-6">Total: €${order.totalPrice.toFixed(2)}</p>
            <div id="checkout-form-container" class="border p-6 rounded shadow-md bg-white">
                ${getFormHtml()}
            </div>
        `;

        const form = el.querySelector<HTMLFormElement>("form")!;
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const dto: PlaceOrderDto = formDataToDto(form);
            const {valid, errors} = validateOrderDto(dto);

            Object.keys(errors).forEach(field => {
                const errorEl = form.querySelector<HTMLElement>(`[data-error="${field}"]`)!;
                if (errors[field]) errorEl.classList.remove("hidden");
                else errorEl.classList.add("hidden");
            });

            if (!valid) return;

            if (!orderId) {
                form.querySelector("#form-error")!.textContent = "Order ID missing.";
                form.querySelector("#form-error")!.classList.remove("hidden");
                return;
            }

            try {
                const placedOrder = await setCustomerDetails(orderId, dto);
                const paymentOrder = await createPayment(orderId);

                resetCurrentOrder();

                if (paymentOrder.paymentUrl) {
                    window.location.href = paymentOrder.paymentUrl;
                } else {
                    window.history.pushState({}, "", `/order/${placedOrder.id}`);
                    window.dispatchEvent(new PopStateEvent("popstate"));
                }
            } catch (err: any) {
                form.querySelector("#form-error")!.textContent = err?.message || "Failed to continue to payment. Please try again.";
                form.querySelector("#form-error")!.classList.remove("hidden");
            }
        });
    }

    function formDataToDto(form: HTMLFormElement): PlaceOrderDto {
        const data = new FormData(form);
        return {
            name: data.get("name") as string,
            email: data.get("email") as string,
            addressDto: {
                street: data.get("street") as string,
                houseNumber: data.get("houseNumber") as string,
                busNumber: data.get("busNumber") as string || null,
                city: data.get("city") as string,
                country: data.get("country") as string,
                postalCode: data.get("postalCode") as string
            }
        };
    }

    function getFormHtml() {
        return `
            <form class="space-y-4">
                <h2 class="text-2xl font-semibold mb-4 text-center">Enter Your Details</h2>
                <div id="form-error" class="text-red-500 font-semibold hidden mb-2"></div>
                ${[
            {label: "Name", name: "name", type: "text", required: true},
            {label: "Email", name: "email", type: "email", required: true},
            {label: "Street", name: "street", type: "text", required: true},
            {label: "House Number", name: "houseNumber", type: "text", required: true},
            {label: "Bus Number", name: "busNumber", type: "text"},
            {label: "City", name: "city", type: "text", required: true},
            {label: "Country", name: "country", type: "text", required: true},
            {label: "Postal Code", name: "postalCode", type: "text", required: true, pattern: "^[1-9][0-9]{3}$"}
        ].map(f => `
                    <div>
                        <label class="block font-medium mb-1">${f.label}</label>
                        <input type="${f.type}" name="${f.name}" class="w-full border rounded px-3 py-2" ${f.required ? "required" : ""} ${f.pattern ? `pattern="${f.pattern}"` : ""}>
                        <p class="text-red-500 text-sm hidden" data-error="${f.name}">${f.label} is required</p>
                    </div>
                `).join("")}
                <button type="submit" class="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 rounded mt-4 text-lg font-semibold">Continue to Payment</button>
            </form>
        `;
    }

    return el;
}

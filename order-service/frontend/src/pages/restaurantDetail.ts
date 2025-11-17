import {getDishesByRestaurant, getRestaurant} from "../infrastructure/restaurantRest";
import {addToCard} from "../infrastructure/orderRest";
import {getCurrentOrder, getOrCreateOrder, setCurrentOrder, subscribeOrder} from "../service/orderServices.ts";
import type {Order} from "../domain/order";

export default function RestaurantPage(params?: Record<string, string>): HTMLElement {
    const el = document.createElement("div");
    const restaurantId = params?.id;
    el.className = "max-w-5xl mx-auto mt-8 px-4";

    if (!restaurantId) {
        return el;
    }

    const infoEl = document.createElement("div");
    const dishesEl = document.createElement("div");
    dishesEl.className = "grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mt-6";

    el.append(infoEl, dishesEl);

    const cartEl = document.createElement("div");
    cartEl.className = "fixed top-4 right-4 flex gap-2 z-50";

    const checkoutBtn = document.createElement("button");
    checkoutBtn.className = "bg-green-600 text-white px-4 py-2 rounded-full shadow hover:bg-green-700 transition";
    checkoutBtn.textContent = "Checkout";
    checkoutBtn.onclick = () => {
        const order = getCurrentOrder();
        if (order && order.lines.length > 0) {
            window.history.pushState({}, "", `/order/checkout/${order.id}`);
            window.dispatchEvent(new PopStateEvent("popstate"));
        }
    };

    const cartBtn = document.createElement("button");
    cartBtn.className = "bg-blue-600 text-white px-4 py-2 rounded-full shadow hover:bg-blue-700 transition";

    cartEl.append(checkoutBtn, cartBtn);
    document.body.appendChild(cartEl);

    function updateCart(order: Order | null) {
        const count = order?.lines?.reduce((s, l) => s + l.quantity, 0) ?? 0;
        cartBtn.textContent = `Cart (${count})`;

        checkoutBtn.style.display = count > 0 ? "block" : "none";
    }

    cartBtn.onclick = () => {
        const order = getCurrentOrder();
        if (order) {
            window.history.pushState({}, "", `/order/checkout/${order.id}`);
            window.dispatchEvent(new PopStateEvent("popstate"));
        }
    };

    subscribeOrder(updateCart);
    updateCart(getCurrentOrder());

    (async () => {
        try {
            if (!restaurantId) {
                el.innerHTML = `<p class="text-red-500 text-center">No restaurant ID provided.</p>`;
                return;
            }

            const restaurant = await getRestaurant(restaurantId);
            if (!restaurant) {
                infoEl.innerHTML = `<p class="text-red-500 text-center">Restaurant not found.</p>`;
                return;
            }

            infoEl.innerHTML = `
        <a href="/" class="text-blue-600 hover:underline text-sm">← Back</a>
        <h1 class="text-3xl font-bold mt-4">${restaurant.name}</h1>
        <p class="text-gray-600">${restaurant.address.street} ${restaurant.address.houseNumber}, ${restaurant.address.city}</p>
        <span class="text-sm ${restaurant.isOpen ? "text-green-600" : "text-red-500"}">
          ${restaurant.isOpen ? "Open" : "Closed"}
        </span>
      `;

            const dishes = await getDishesByRestaurant(restaurantId);
            dishesEl.innerHTML = dishes
                .map(
                    (d) => `
          <div class="bg-white rounded-2xl shadow p-4 flex flex-col">
            <div class="flex-1">
              <h3 class="font-semibold text-lg mb-1">${d.name}</h3>
              <p class="text-sm text-gray-600 mb-2">${d.description ?? ""}</p>
              <p class="font-medium mb-2">€${d.price.toFixed(2)}</p>
              <input type="number" value="1" min="1" class="quantity border rounded w-full px-2 py-1 mb-2">
              <input type="text" placeholder="Notes..." class="notes border rounded w-full px-2 py-1 mb-2">
            </div>
            <button class="add-to-cart bg-blue-600 text-white py-2 rounded mt-2 hover:bg-blue-700" data-id="${d.id}">
              Add to Cart
            </button>
          </div>
        `
                )
                .join("");
        } catch {
            infoEl.innerHTML = `<p class="text-center text-red-500">Failed to load restaurant.</p>`;
        }
    })();

    el.addEventListener("click", async (e) => {
        const btn = (e.target as HTMLElement).closest(".add-to-cart") as HTMLButtonElement;
        if (!btn) return;

        const card = btn.closest("div")!;
        const quantity = Number((card.querySelector(".quantity") as HTMLInputElement).value) || 1;
        const notes = (card.querySelector(".notes") as HTMLInputElement).value || undefined;

        btn.disabled = true;
        btn.textContent = "Adding...";

        try {
            const orderId = await getOrCreateOrder();
            const updated = await addToCard(orderId, {
                restaurantId,
                dishId: btn.dataset.id!,
                quantity,
                notes,
            });
            setCurrentOrder(updated);
            btn.textContent = "✅ Added";
        } catch {
            btn.textContent = "❌ Error";
        } finally {
            setTimeout(() => {
                btn.textContent = "Add to Cart";
                btn.disabled = false;
            }, 1000);
        }
    });

    return el;
}
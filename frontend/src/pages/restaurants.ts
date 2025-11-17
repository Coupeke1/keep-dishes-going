import type { Restaurant } from "../domain/restaurant.ts";
import { getRestaurants } from "../infrastructure/restaurantRest.ts";

export default function RestaurantsPage(): HTMLElement {
  const el = document.createElement("div");

  el.innerHTML = `
    <div class="text-center mt-8 px-4">
      <h1 class="text-3xl font-bold text-gray-800 mb-2">Keep Dishes going</h1>
      <p class="text-gray-500 mb-6">Discover nearby places to eat!</p>
      <div id="restaurant-list" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto"></div>
    </div>
  `;

  setTimeout(async () => {
    const list = el.querySelector("#restaurant-list")!;
    list.innerHTML = `<p class="text-gray-400 text-center animate-pulse">Loading restaurants...</p>`;

    try {
      const restaurants: Restaurant[] = await getRestaurants();
      console.log(restaurants)
      if (restaurants.length === 0) {
        list.innerHTML = `<p class="text-gray-500 text-center">No restaurants found.</p>`;
        return;
      }

      list.innerHTML = restaurants
        .map(r => {
          return `
          <a href="/restaurants/${r.id}">
          <div class="bg-white shadow-md rounded-2xl p-5 hover:shadow-lg hover:scale-105 transition duration-200">
            <div class="flex justify-between items-start mb-3">
              <h2 class="text-xl font-semibold text-gray-800">${r.name}</h2>
              <span class="px-3 py-1 rounded-full text-sm font-medium ${
                r.isOpen ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"
              }">${r.isOpen ? "Open" : "Closed"}</span>
            </div>
            <p class="text-sm text-gray-500 mb-2">${r.address.street} ${r.address.houseNumber}${r.address.busNumber}</p>
            <p class="text-sm text-gray-500 mb-2">${r.address.postalCode} ${r.address.city}</p>
            <p class="text-sm text-gray-600 mb-1">
              🍽️ <span class="font-medium">${r.cuisineType}</span>
            </p>
            <p class="text-sm text-gray-600 mb-1">
              ${r.priceIndicator}
            </p>
          </div>
          </a>
          `;
        })
        .join("");
    } catch (err) {
      console.error(err);
      list.innerHTML = `<p class="text-red-500 text-center">Failed to load restaurants.</p>`;
    }
  });

  return el;
}
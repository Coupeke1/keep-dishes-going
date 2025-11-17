import type {Restaurant} from "../domain/restaurant.ts";
import type {Dish} from "../domain/dish.ts";

const SERVER_URL = "http://localhost:8081/api/";
const RESTAURANT_URL = SERVER_URL + "restaurants";
export async function getRestaurants(): Promise<Restaurant[]> {
    const response = await fetch(RESTAURANT_URL, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        }
    });
    if (!response.ok) {
        return Promise.resolve([]);
    }
    return response.json();
}

export async function getRestaurant(restaurantId: string): Promise<Restaurant | null> {
    const response = await fetch(`${RESTAURANT_URL}/${restaurantId}`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json"
        }
    });
    if (!response.ok) {
        return Promise.resolve(null);
    }
    return response.json();
}

export async function getDishesByRestaurant(restaurantId: string): Promise<Dish[]> {
    const response = await fetch(`${RESTAURANT_URL}/${restaurantId}/dishes`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json"
        }
    });
    if (!response.ok) {
        return Promise.resolve([]);
    }
    return response.json();
}


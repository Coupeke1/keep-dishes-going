import type {Order} from "../domain/order.ts";

const SERVER_URL = "http://localhost:8081/api/";
const ORDER_URL = SERVER_URL + "orders";

export async function getOrder(orderId: string): Promise<Order> {
    const response = await fetch(`${ORDER_URL}/${orderId}`, {
        method: "GET",
        headers: { "Content-Type": "application/json" },
    });
    if (!response.ok) throw new Error("Failed to find order");
    return response.json();
}

export async function createOrder(): Promise<Order> {
    const response = await fetch(ORDER_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
    });
    if (!response.ok) throw new Error("Failed to create order");
    return response.json();
}

export async function addToCard(orderId: string, data: {
    restaurantId: string;
    dishId: string;
    quantity: number;
    notes?: string;
}): Promise<Order> {
    const response = await fetch(`${ORDER_URL}/${orderId}/dishes`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
    });
    if (!response.ok) throw new Error("Failed to add dish to order");
    return response.json();
}

export interface PlaceOrderDto {
    name: string;
    email: string;
    addressDto: {
        street: string;
        houseNumber: string;
        busNumber?: string | null;
        city: string;
        country: string;
        postalCode: string;
    };
}

export async function setCustomerDetails(orderId: string, dto: PlaceOrderDto): Promise<Order> {
    const response = await fetch(`${ORDER_URL}/${orderId}/customer-details`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
    });

    if (!response.ok) {
        let errorMsg = "Failed to place order";
        try {
            const errJson = await response.json();
            errorMsg = errJson.message || errorMsg;
        } catch {}
        throw new Error(errorMsg);
    }

    return response.json();
}

export async function createPayment(orderId: string): Promise<Order> {
    const response = await fetch(`${ORDER_URL}/${orderId}/create-payment`, {
        method: "POST",
        headers: { "Content-Type": "application/json" }
    });

    if (!response.ok) {
        let errorMsg = "Failed to create payment";
        try {
            const errJson = await response.json();
            errorMsg = errJson.message || errorMsg;
        } catch {}
        throw new Error(errorMsg);
    }

    return response.json();
}

export async function verifiyPayment(orderId: string) {
    const response = await fetch(`${ORDER_URL}/payments/webhook?order-id=${orderId}`, {
        method: "POST"
    });

    if (!response.ok) {
        let errorMsg = "Failed to verify payment";
        try {
            const errJson = await response.json();
            errorMsg = errJson.message || errorMsg;
        } catch {}
        throw new Error(errorMsg);
    }
}
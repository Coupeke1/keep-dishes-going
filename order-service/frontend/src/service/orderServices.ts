import {createOrder, getOrder, type PlaceOrderDto} from "../infrastructure/orderRest";
import type {Order} from "../domain/order";

let currentOrder: Order | null = null;
const subs: ((order: Order | null) => void)[] = [];

export async function getOrCreateOrder(): Promise<string> {
    if (currentOrder && currentOrder.status === "CART") {
        return currentOrder.id;
    }

    const savedId = localStorage.getItem("orderId");
    if (savedId) {
        try {
            const order = await getOrder(savedId);
            if (order?.status === "CART") {
                currentOrder = order;
                notifySubscribers();
                return order.id;
            }
        } catch (e) {
            console.warn("Failed to restore saved order:", e);
        }
    }

    const newOrder = await createOrder();
    currentOrder = { ...newOrder, lines: newOrder.lines ?? [] };
    localStorage.setItem("orderId", newOrder.id);
    notifySubscribers();
    return newOrder.id;
}

export async function ensureCurrentOrder() {
    if (currentOrder) return currentOrder;
    const savedId = localStorage.getItem("orderId");
    if (!savedId) return null;
    try {
        const order = await getOrder(savedId);
        currentOrder = order;
        notifySubscribers();
        return order;
    } catch {
        return null;
    }
}

export function validateOrderDto(dto: PlaceOrderDto): { valid: boolean; errors: Record<string, string> } {
    const errors: Record<string, string> = {};

    if (!dto.name) errors.name = "Name is required";
    if (!dto.email || !/\S+@\S+\.\S+/.test(dto.email)) errors.email = "Valid email is required";
    if (!dto.addressDto.street) errors.street = "Street is required";
    if (!dto.addressDto.houseNumber) errors.houseNumber = "House number is required";
    if (!dto.addressDto.city) errors.city = "City is required";
    if (!dto.addressDto.country) errors.country = "Country is required";
    if (!dto.addressDto.postalCode || !/^[1-9][0-9]{3}$/.test(dto.addressDto.postalCode))
        errors.postalCode = "Postal code must be a valid Belgian code";

    return { valid: Object.keys(errors).length === 0, errors };
}

export function getCurrentOrder() {
    return currentOrder;
}

export function setCurrentOrder(o: Order) {
    currentOrder = o;
    localStorage.setItem("orderId", o.id);
    notifySubscribers();
}

export function resetCurrentOrder() {
    currentOrder = null;
    localStorage.removeItem("orderId");
    notifySubscribers();
}

export function subscribeOrder(fn: (order: Order | null) => void) {
    subs.push(fn);
    fn(currentOrder);
}

function notifySubscribers() {
    subs.forEach((fn) => fn(currentOrder));
}
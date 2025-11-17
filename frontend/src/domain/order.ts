export type OrderStatus = "CART" | "CUSTOMER_DETAILS_PROVIDED" | "PAYMENT_IN_PROGRESS" | "PLACED" | "ACCEPTED" | "READY" | "PICKED_UP" | "DELIVERED" | "CANCELLED" | "REJECTED";

export interface Order {
    id: string;
    restaurantId?: string;
    status: OrderStatus;
    lines: OrderLine[];
    totalPrice: number;
    paymentUrl: string;
}

export interface OrderLine {
    dishId: string;
    dishName: string;
    unitPrice: number;
    totalPrice: number;
    quantity: number;
    notes: string;
}
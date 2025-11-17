import type {Address} from "./address.ts";

export interface Restaurant {
    id: string;
    name: string;
    address: Address;
    cuisineType: string;
    priceIndicator: string;
    openingHours: OpeningHours;
    isOpen: boolean;
}

export interface OpeningHours {
    days: {
        day: string;
        openingPeriods: {
          openTime: string
          closeTime: string
        }[]
    }[]
}
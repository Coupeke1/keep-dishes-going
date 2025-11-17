export type DishCategory = "STARTER" | "MAIN" | "DESERT";
export type DishStatus = "AVAILABLE" | "SOLD_OUT"

export interface Dish {
    id: string;
    name: string;
    description?: string;
    price: number;
    vegetarian: boolean;
    vegan: boolean;
    glutenFree: boolean;
    category: DishCategory;
    status: DishStatus;
}
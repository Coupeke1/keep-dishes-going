import { router } from "./router";
import {ensureCurrentOrder} from "./service/orderServices.ts";

document.addEventListener("DOMContentLoaded", async () => {
    await ensureCurrentOrder();
    router();
})
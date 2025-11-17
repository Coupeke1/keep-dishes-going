import RestaurantsPage from "./pages/restaurants.ts";
import RestaurantDetailPage from "./pages/restaurantDetail.ts";
import OrderCheckoutPage from "./pages/OrderCheckOut.ts";
import OrderTrackingPage from "./pages/OrderTracking.ts";

const routes: Record<string, (params?: Record<string, string>) => HTMLElement> = {
    "/": RestaurantsPage,
    "/restaurants": RestaurantsPage,
    "/restaurants/:id": RestaurantDetailPage,
    "/order/checkout/:id": OrderCheckoutPage,
    "/order/:id": OrderTrackingPage,
};

function matchRoute(pathname: string) {
    for (const route in routes) {
        const paramNames: string[] = [];
        const regexPath = route.replace(/:([^/]+)/g, (_, key) => {
            paramNames.push(key);
            return "([^/]+)";
        });
        const regex = new RegExp(`^${regexPath}$`);
        const match = pathname.match(regex);
        if (match) {
            const params: Record<string, string> = {};
            paramNames.forEach((name, i) => (params[name] = match[i + 1]));
            return { route, params };
        }
    }
    return null;
}

export function router() {
    const path = window.location.pathname;
    const app = document.getElementById("app")!;
    app.innerHTML = "";

    const match = matchRoute(path);
    if (match) {
        const Page = routes[match.route];
        app.appendChild(Page(match.params));
    } else {
        // fallback
        app.appendChild(RestaurantsPage());
    }
}

// Browser navigation
window.addEventListener("popstate", router);

// Link interception
document.addEventListener("click", (e) => {
    const target = e.target as HTMLElement;
    const anchor = target.closest("a");
    if (anchor && anchor.getAttribute("href")?.startsWith("/")) {
        e.preventDefault();
        const href = anchor.getAttribute("href");
        if (href) {
            window.history.pushState({}, "", href);
            router();
        }
    }
});
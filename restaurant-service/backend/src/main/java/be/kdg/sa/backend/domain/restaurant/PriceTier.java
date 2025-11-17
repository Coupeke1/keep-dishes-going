package be.kdg.sa.backend.domain.restaurant;

public enum PriceTier {
    CHEAP("€", 10),
    MODERATE("€€", 30),
    EXPENSIVE("€€€", 60),
    LUXURY("€€€€", Integer.MAX_VALUE);

    private final String symbol;
    private final int maxPrice;

    PriceTier(String symbol, int maxPrice) {
        this.symbol = symbol;
        this.maxPrice = maxPrice;
    }

    public static PriceTier from(double price) {
        for (PriceTier tier : values()) {
            if (price <= tier.maxPrice) return tier;
        }
        return LUXURY;
    }

    public String symbol() {
        return symbol;
    }
}

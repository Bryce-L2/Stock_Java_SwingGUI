package com.stocksim;

import java.util.HashMap;
import java.util.Map;

public class Portfolio {
    private double cash;
    private final Map<String, Integer> holdings = new HashMap<>();
    private final Map<String, Double> avgBuyPrice = new HashMap<>();

    public Portfolio(double startingCash) {
        this.cash = startingCash;
    }

    public boolean buy(String ticker, int shares, double price) {
        double cost = shares * price;
        if (cost > cash) return false;
        cash -= cost;
        int current = holdings.getOrDefault(ticker, 0);
        double currentAvg = avgBuyPrice.getOrDefault(ticker, 0.0);
        double newAvg = ((current * currentAvg) + (shares * price)) / (current + shares);
        holdings.put(ticker, current + shares);
        avgBuyPrice.put(ticker, newAvg);
        return true;
    }

    public boolean sell(String ticker, int shares, double price) {
        int current = holdings.getOrDefault(ticker, 0);
        if (shares > current) return false;
        cash += shares * price;
        int remaining = current - shares;
        if (remaining == 0) {
            holdings.remove(ticker);
            avgBuyPrice.remove(ticker);
        } else {
            holdings.put(ticker, remaining);
        }
        return true;
    }

    public double getCash() { return cash; }
    public Map<String, Integer> getHoldings() { return holdings; }
    public double getAvgBuyPrice(String ticker) { return avgBuyPrice.getOrDefault(ticker, 0.0); }

    public double getTotalValue(Map<String, Double> currentPrices) {
        double total = cash;
        for (Map.Entry<String, Integer> e : holdings.entrySet()) {
            Double price = currentPrices.get(e.getKey());
            if (price != null) total += e.getValue() * price;
        }
        return total;
    }
}

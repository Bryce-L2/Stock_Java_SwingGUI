# Stock Market Simulator (StockSim)

A Java Swing desktop app for **paper trading** — practice buying and selling stocks with $10,000 in virtual cash, using real market data, before risking real money.

Search any ticker, trade at live (15-minute delayed) prices from the Alpha Vantage API, and track your portfolio's value and profit/loss in real time.

## Features

- $10,000 starting virtual balance
- Search any stock ticker and pull its live price
- Buy and sell shares with instant balance updates
- Portfolio tab showing holdings, current value, and P&L
- Built with Maven for clean dependency management

## How it works

The app is built around three pieces of state that always stay in sync: a **cash balance**, a **holdings map** (ticker → shares), and a **transaction history**. Every trade routes through dedicated methods that update all three together, the same way a real brokerage account tracks positions.

## Tech

Java · Java Swing · Maven · Alpha Vantage API

## Setup

1. Clone the repo and open it in your IDE (or build with Maven).
2. Get a free API key from [Alpha Vantage](https://www.alphavantage.co/support/#api-key).
3. Add your key where the app reads it (config field / constant).

```bash
mvn clean package
java -jar target/StockSim.jar
```

> Adjust the jar name / main class to match your build.

## Notes

The Alpha Vantage free tier allows only 25 API calls per day, and each search uses two (price + chart history). I designed the request flow to cache where possible and show a clear error state when the limit is hit, so the app never silently fails.

This is a personal learning project, **not financial advice**.

---

**Bryce Lombardo** · [@Bryce-L2](https://github.com/Bryce-L2)

# Stock Market Simulator

A Java Swing paper trading simulator using real market data from Alpha Vantage.

## Features
- Start with $10,000 in virtual cash
- Search any stock by ticker symbol (e.g. AAPL, TSLA, MSFT)
- Buy and sell shares at real (15-min delayed) prices
- 30-day price history chart with dark theme
- Portfolio tracker showing holdings, current value, and P&L

## Setup

### Prerequisites
- Java 11+
- Maven 3.6+

### 1. Add your API key
Edit the `.env` file and replace `your_api_key_here` with your Alpha Vantage key:
```
ALPHA_VANTAGE_KEY=ABC123XYZ
```
Get a free key at: https://www.alphavantage.co/support/#api-key

Alternatively, the app will prompt you for the key on launch.

### 2. Build
```bash
mvn clean package
```

### 3. Run
```bash
java -jar target/StockSimulator.jar
```

## Notes
- Free Alpha Vantage tier: 25 API calls/day (each search uses 2 calls — price + chart)
- Prices are 15-20 minutes delayed
- Portfolio data is not saved between sessions

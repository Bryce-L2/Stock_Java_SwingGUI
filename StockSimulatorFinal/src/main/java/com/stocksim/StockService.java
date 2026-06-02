package com.stocksim;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class StockService {

    private final String apiKey;

    private static final String BASE =
            "https://www.alphavantage.co/query";

    public StockService(String apiKey) {
        this.apiKey = apiKey;
    }

    private String fetch(String urlStr) throws Exception {

        URL url = new URL(urlStr);

        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        BufferedReader in =
                new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

        StringBuilder sb = new StringBuilder();

        String line;

        while ((line = in.readLine()) != null) {
            sb.append(line);
        }

        in.close();

        return sb.toString();
    }

    // Current stock price
    public double getCurrentPrice(String ticker) throws Exception {

        String url =
                BASE +
                "?function=GLOBAL_QUOTE" +
                "&symbol=" + ticker +
                "&apikey=" + apiKey;

        String raw = fetch(url);

        System.out.println("QUOTE RESPONSE:");
        System.out.println(raw);

        JSONObject json = new JSONObject(raw);

        // Handle API issues
        if (json.has("Note")) {
            throw new Exception(
                    "API rate limit hit. Please wait 1 minute."
            );
        }

        if (json.has("Information")) {
            throw new Exception(
                    json.getString("Information")
            );
        }

        if (json.has("Error Message")) {
            throw new Exception(
                    "Invalid ticker symbol."
            );
        }

        JSONObject quote =
                json.optJSONObject("Global Quote");

        if (quote == null || !quote.has("05. price")) {
            throw new Exception(
                    "Ticker not found: " + ticker
            );
        }

        return Double.parseDouble(
                quote.getString("05. price")
        );
    }

    // Company name lookup
    public String getCompanyName(String ticker) throws Exception {

        String url =
                BASE +
                "?function=SYMBOL_SEARCH" +
                "&keywords=" + ticker +
                "&apikey=" + apiKey;

        String raw = fetch(url);

        JSONObject json = new JSONObject(raw);

        if (json.has("bestMatches")) {

            var arr = json.getJSONArray("bestMatches");

            if (arr.length() > 0) {

                return arr
                        .getJSONObject(0)
                        .optString("2. name", ticker);
            }
        }

        return ticker;
    }

    // Last 30 days of history
    public Map<LocalDate, Double> getDailyHistory(String ticker)
            throws Exception {

        String url =
                BASE +
                "?function=TIME_SERIES_DAILY" +
                "&symbol=" + ticker +
                "&outputsize=compact" +
                "&apikey=" + apiKey;

        String raw = fetch(url);

        System.out.println("HISTORY RESPONSE:");
        System.out.println(raw);

        JSONObject json = new JSONObject(raw);

        // Handle API issues
        if (json.has("Note")) {
            throw new Exception(
                    "API rate limit hit. Wait 1 minute and try again."
            );
        }

        if (json.has("Information")) {
            throw new Exception(
                    json.getString("Information")
            );
        }

        if (json.has("Error Message")) {
            throw new Exception(
                    "Invalid ticker symbol."
            );
        }

        JSONObject series =
                json.optJSONObject("Time Series (Daily)");

        if (series == null) {
            throw new Exception(
                    "No history data for: " + ticker
            );
        }

        LinkedHashMap<LocalDate, Double> result =
                new LinkedHashMap<>();

        series.keySet()
                .stream()
                .sorted()
                .skip(Math.max(
                        0,
                        series.keySet().size() - 30
                ))
                .forEach(dateStr -> {

                    JSONObject day =
                            series.getJSONObject(dateStr);

                    result.put(
                            LocalDate.parse(dateStr),
                            Double.parseDouble(
                                    day.getString("4. close")
                            )
                    );
                });

        System.out.println("PARSED HISTORY:");
        System.out.println(result);

        return result;
    }
}
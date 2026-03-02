package nd.com.demo.service;

import nd.com.demo.model.StockResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class StockService {

    private final RestTemplate restTemplate;

    @Value("${alphavantage.api.key}")
    private String apiKey;

    // -------- Smart Cache (60 sec expiry) --------
    private static class CachedStock {
        StockResponse response;
        long timestamp;

        CachedStock(StockResponse response) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private final Map<String, CachedStock> cache = new HashMap<>();

    public StockService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public StockResponse analyze(String symbol) {

        try {

            symbol = symbol.toUpperCase();

            // -------- Check Cache --------
            if (cache.containsKey(symbol)) {
                CachedStock cached = cache.get(symbol);
                long age = (System.currentTimeMillis() - cached.timestamp) / 1000;

                if (age < 60) {
                    return cached.response;
                }
            }

            // -------- FREE API (DAILY) --------
            String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY"
                    + "&symbol=" + symbol
                    + "&apikey=" + apiKey;

            String response = restTemplate.getForObject(url, String.class);
            JSONObject jsonObject = new JSONObject(response);

            // -------- API limit / error handling --------
            if (jsonObject.has("Note") || jsonObject.has("Error Message")
                    || !jsonObject.has("Time Series (Daily)")) {

                return new StockResponse(
                        symbol, 0, 0, 0, 0,
                        "No Data",
                        "API Limit or Invalid Symbol",
                        0,
                        "HOLD",
                        50,
                        50
                );
            }

            JSONObject timeSeries =
                    jsonObject.getJSONObject("Time Series (Daily)");

            List<String> dates =
                    new ArrayList<>(timeSeries.keySet());

            Collections.sort(dates, Collections.reverseOrder());

            List<Double> closePrices = new ArrayList<>();

            for (int i = 0; i < 100 && i < dates.size(); i++) {
                JSONObject candle =
                        timeSeries.getJSONObject(dates.get(i));
                closePrices.add(candle.getDouble("4. close"));
            }

            if (closePrices.size() < 20) {
                return new StockResponse(
                        symbol, 0,0,0,0,
                        "Insufficient Data",
                        "Not enough history",
                        0,
                        "HOLD",
                        50,
                        50
                );
            }

            double latestPrice = closePrices.get(0);
            double ma20 = calculateMA(closePrices, 20);
            double ma50 = calculateMA(closePrices, 50);
            double rsi = calculateRSI(closePrices, 14);

            // -------- Trend --------
            String trend;
            if (ma20 > ma50) trend = "Golden Cross (Bullish)";
            else if (ma20 < ma50) trend = "Death Cross (Bearish)";
            else trend = "Sideways";

            // -------- Signal --------
            String signal;
            if (rsi < 30) signal = "STRONG BUY";
            else if (rsi > 70) signal = "STRONG SELL";
            else if (latestPrice > ma20) signal = "BUY";
            else signal = "SELL";

            int confidence =
                    calculateConfidence(latestPrice, ma20, ma50, rsi);

            // -------- AI Probability Engine --------
            int buyScore = 0;
            int sellScore = 0;

            if (rsi < 30) buyScore += 30;
            if (rsi > 70) sellScore += 30;

            if (ma20 > ma50) buyScore += 25;
            else sellScore += 25;

            if (latestPrice > ma20) buyScore += 20;
            else sellScore += 20;

            if (trend.contains("Bullish")) buyScore += 15;
            if (trend.contains("Bearish")) sellScore += 15;

            int totalScore = buyScore + sellScore;

            int buyProbability =
                    totalScore == 0 ? 50 : (buyScore * 100) / totalScore;

            int sellProbability =
                    totalScore == 0 ? 50 : (sellScore * 100) / totalScore;

            String aiSuggestion;
            if (buyProbability > 60) aiSuggestion = "BUY";
            else if (sellProbability > 60) aiSuggestion = "SELL";
            else aiSuggestion = "HOLD";

            // -------- Price History (30 days) --------
            List<Double> priceHistory =
                    new ArrayList<>(closePrices.subList(
                            0,
                            Math.min(30, closePrices.size())
                    ));
            Collections.reverse(priceHistory);

            // -------- RSI History --------
            List<Double> rsiHistory = new ArrayList<>();
            for (int i = 0; i < 30 && i + 14 < closePrices.size(); i++) {
                List<Double> sub =
                        closePrices.subList(i, i + 15);
                rsiHistory.add(calculateRSI(sub, 14));
            }
            Collections.reverse(rsiHistory);

            StockResponse stockResponse =
                    new StockResponse(
                            symbol,
                            latestPrice,
                            ma20,
                            ma50,
                            rsi,
                            trend,
                            signal,
                            confidence,
                            aiSuggestion,
                            buyProbability,
                            sellProbability
                    );

            stockResponse.priceHistory = priceHistory;
            stockResponse.rsiHistory = rsiHistory;

            cache.put(symbol, new CachedStock(stockResponse));

            return stockResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return new StockResponse(
                    symbol, 0,0,0,0,
                    "Error",
                    "Exception Occurred",
                    0,
                    "HOLD",
                    50,
                    50
            );
        }
    }

    // ---------------- Utilities ----------------

    private double calculateMA(List<Double> prices, int period) {
        if (prices.size() < period) return 0;

        double sum = 0;
        for (int i = 0; i < period; i++)
            sum += prices.get(i);

        return sum / period;
    }

    private double calculateRSI(List<Double> prices, int period) {
        if (prices.size() < period + 1) return 0;

        double gain = 0;
        double loss = 0;

        for (int i = 0; i < period; i++) {
            double change =
                    prices.get(i) - prices.get(i + 1);

            if (change > 0) gain += change;
            else loss += Math.abs(change);
        }

        double avgGain = gain / period;
        double avgLoss = loss / period;

        if (avgLoss == 0) return 100;

        double rs = avgGain / avgLoss;

        return 100 - (100 / (1 + rs));
    }

    private int calculateConfidence(
            double price,
            double ma20,
            double ma50,
            double rsi
    ) {

        int score = 0;

        if (price > ma20) score += 25;
        if (ma20 > ma50) score += 25;
        if (rsi > 40 && rsi < 60) score += 25;
        if (rsi < 30 || rsi > 70) score += 15;

        return score;
    }
}
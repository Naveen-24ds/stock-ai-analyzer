package nd.com.demo.model;

import java.util.List;

public class StockResponse {

    public String symbol;
    public double latestPrice;
    public double ma20;
    public double ma50;
    public double rsi;
    public String trend;
    public String signal;
    public int confidence;

    public String aiSuggestion;
    public int buyProbability;
    public int sellProbability;

    public List<Double> priceHistory;
    public List<Double> rsiHistory;

    public StockResponse(String symbol,
                         double latestPrice,
                         double ma20,
                         double ma50,
                         double rsi,
                         String trend,
                         String signal,
                         int confidence,
                         String aiSuggestion,
                         int buyProbability,
                         int sellProbability) {

        this.symbol = symbol;
        this.latestPrice = latestPrice;
        this.ma20 = ma20;
        this.ma50 = ma50;
        this.rsi = rsi;
        this.trend = trend;
        this.signal = signal;
        this.confidence = confidence;
        this.aiSuggestion = aiSuggestion;
        this.buyProbability = buyProbability;
        this.sellProbability = sellProbability;
    }
}
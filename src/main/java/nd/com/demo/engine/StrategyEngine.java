package nd.com.demo.engine;

public class StrategyEngine {

    public static String generateSignal(
            double latestPrice,
            double ma20,
            double ma50,
            double rsi
    ) {

        if (rsi < 30 && ma20 > ma50) {
            return "STRONG BUY (Oversold + Bullish Trend)";
        }
        else if (rsi > 70 && ma20 < ma50) {
            return "STRONG SELL (Overbought + Bearish Trend)";
        }
        else if (latestPrice > ma20 && ma20 > ma50) {
            return "BUY";
        }
        else if (latestPrice < ma20 && ma20 < ma50) {
            return "SELL";
        }
        else {
            return "HOLD";
        }
    }
}
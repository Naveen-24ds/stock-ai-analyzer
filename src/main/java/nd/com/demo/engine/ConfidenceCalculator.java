package nd.com.demo.engine;

public class ConfidenceCalculator {

    public static int calculateConfidence(
            double latestPrice,
            double ma20,
            double ma50,
            double rsi
    ) {

        int score = 0;

        if (ma20 > ma50)
            score += 25;

        if (latestPrice > ma20)
            score += 25;

        if (rsi > 40 && rsi < 60)
            score += 20;

        if (rsi < 30 || rsi > 70)
            score += 30;

        return score;
    }
}
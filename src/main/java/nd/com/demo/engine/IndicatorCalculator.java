package nd.com.demo.engine;

import java.util.List;

public class IndicatorCalculator {

    public static double calculateMovingAverage(List<Double> prices) {

        double sum = 0.0;

        for (double price : prices) {
            sum += price;
        }

        return sum / prices.size();
    }

    public static double calculateRSI(List<Double> prices, int period) {

        double gain = 0.0;
        double loss = 0.0;

        for (int i = 0; i < period; i++) {

            double today = prices.get(i);
            double yesterday = prices.get(i + 1);

            double change = today - yesterday;

            if (change > 0) {
                gain += change;
            } else {
                loss += Math.abs(change);
            }
        }

        double avgGain = gain / period;
        double avgLoss = loss / period;

        if (avgLoss == 0)
            return 100;

        double rs = avgGain / avgLoss;

        return 100 - (100 / (1 + rs));
    }
}
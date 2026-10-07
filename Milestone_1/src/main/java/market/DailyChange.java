package market;

import java.time.LocalDate;

/**
 * How much a stock's closing price changed on one trading day, compared with
 * the trading day before it. Used to find the best and worst trading days.
 */
public class DailyChange {

    private final LocalDate date;
    private final double percentChange;

    public DailyChange(LocalDate date, double percentChange) {
        this.date = date;
        this.percentChange = percentChange;
    }

    public LocalDate getDate() {
        return date;
    }

    /** For example 2.5 means the price went up 2.5%; -1.2 means it went down 1.2%. */
    public double getPercentChange() {
        return percentChange;
    }
}

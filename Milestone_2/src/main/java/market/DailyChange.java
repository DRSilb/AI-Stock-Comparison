package market;

import java.time.LocalDate;

/**
 * Finds the change in stock price from one day to the next.
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

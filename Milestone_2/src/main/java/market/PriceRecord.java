package market;

import java.time.LocalDate;

/**
 * One day of trading data for a stock (one line of a CSV file).
 *
 * The fields are final, so a PriceRecord cannot be changed after it is created.
 * It implements Comparable so a list of records can be sorted by date with
 * Collections.sort().
 */
public class PriceRecord implements Comparable<PriceRecord> {

    private final LocalDate date;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final long volume;

    public PriceRecord(LocalDate date, double open, double high, double low, double close, long volume) {
        if (open <= 0 || high <= 0 || low <= 0 || close <= 0) {
            throw new IllegalArgumentException("Prices must be greater than zero");
        }
        if (volume < 0) {
            throw new IllegalArgumentException("Volume cannot be negative");
        }
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public long getVolume() {
        return volume;
    }

    /** Orders records by date, oldest first. */
    @Override
    public int compareTo(PriceRecord other) {
        return this.date.compareTo(other.date);
    }
}

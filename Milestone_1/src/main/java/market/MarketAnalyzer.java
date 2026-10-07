package market;

import java.util.ArrayList;
import java.util.List;

/**
 * Does all the Milestone 1 calculations.
 *
 * Every method takes the price records for the selected period (oldest first)
 * and returns one result, so each requirement maps to exactly one method.
 * This class does not print anything; MarketTerminal displays the results.
 */
public class MarketAnalyzer {

    /** The record with the highest closing price (the earliest one if there is a tie). */
    public PriceRecord findHighestClose(List<PriceRecord> records) {
        checkNotEmpty(records);
        PriceRecord highest = records.get(0);
        for (PriceRecord record : records) {
            if (record.getClose() > highest.getClose()) {
                highest = record;
            }
        }
        return highest;
    }

    /** The record with the lowest closing price (the earliest one if there is a tie). */
    public PriceRecord findLowestClose(List<PriceRecord> records) {
        checkNotEmpty(records);
        PriceRecord lowest = records.get(0);
        for (PriceRecord record : records) {
            if (record.getClose() < lowest.getClose()) {
                lowest = record;
            }
        }
        return lowest;
    }

    public double calculateAverageClose(List<PriceRecord> records) {
        checkNotEmpty(records);
        double sum = 0;
        for (PriceRecord record : records) {
            sum += record.getClose();
        }
        return sum / records.size();
    }

    /** Percentage change from the first closing price to the last closing price. */
    public double calculatePriceChange(List<PriceRecord> records) {
        checkNotEmpty(records);
        double firstClose = records.get(0).getClose();
        double lastClose = records.get(records.size() - 1).getClose();
        return percentChange(firstClose, lastClose);
    }

    public double calculateAverageVolume(List<PriceRecord> records) {
        checkNotEmpty(records);
        double sum = 0;
        for (PriceRecord record : records) {
            sum += record.getVolume();
        }
        return sum / records.size();
    }

    /**
     * The change of each trading day compared with the day before it.
     * The first day in the list has no day before it, so it gets no entry.
     */
    public List<DailyChange> calculateDailyChanges(List<PriceRecord> records) {
        List<DailyChange> changes = new ArrayList<>();
        for (int i = 1; i < records.size(); i++) {
            PriceRecord yesterday = records.get(i - 1);
            PriceRecord today = records.get(i);
            double change = percentChange(yesterday.getClose(), today.getClose());
            changes.add(new DailyChange(today.getDate(), change));
        }
        return changes;
    }

    /** The trading day with the largest percentage increase. Needs at least 2 records. */
    public DailyChange findBestDay(List<PriceRecord> records) {
        List<DailyChange> changes = calculateDailyChanges(records);
        if (changes.isEmpty()) {
            throw new IllegalArgumentException("At least 2 trading days are needed to find the best day");
        }
        DailyChange best = changes.get(0);
        for (DailyChange change : changes) {
            if (change.getPercentChange() > best.getPercentChange()) {
                best = change;
            }
        }
        return best;
    }

    /** The trading day with the largest percentage decrease. Needs at least 2 records. */
    public DailyChange findWorstDay(List<PriceRecord> records) {
        List<DailyChange> changes = calculateDailyChanges(records);
        if (changes.isEmpty()) {
            throw new IllegalArgumentException("At least 2 trading days are needed to find the worst day");
        }
        DailyChange worst = changes.get(0);
        for (DailyChange change : changes) {
            if (change.getPercentChange() < worst.getPercentChange()) {
                worst = change;
            }
        }
        return worst;
    }

    /** Percentage change from one price to another, e.g. 100 -> 110 gives 10.0. */
    private double percentChange(double from, double to) {
        return (to - from) / from * 100;
    }

    private void checkNotEmpty(List<PriceRecord> records) {
        if (records.isEmpty()) {
            throw new IllegalArgumentException("There are no price records in the selected period");
        }
    }
}

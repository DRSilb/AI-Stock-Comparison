package market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class MarketAnalyzerTest {

    private static final double DELTA = 0.0001; // allowed rounding error when comparing doubles

    private final MarketAnalyzer analyzer = new MarketAnalyzer();

    // Closing prices 100 -> 110 (+10%) -> 99 (-10%) -> 99 (0%) -> 120 (+21.21%)
    // Volumes 1000, 2000, 3000, 4000, 5000
    private final List<PriceRecord> records = makeRecords(100, 110, 99, 99, 120);

    /** Creates one record per day starting 01/01/2025 with the given closing prices. */
    private static List<PriceRecord> makeRecords(double... closes) {
        List<PriceRecord> list = new ArrayList<>();
        for (int i = 0; i < closes.length; i++) {
            LocalDate date = LocalDate.of(2025, 1, 1).plusDays(i);
            list.add(new PriceRecord(date, closes[i], closes[i] + 1, closes[i] - 1, closes[i], 1000L * (i + 1)));
        }
        return list;
    }

    @Test
    void highestClose() {
        PriceRecord highest = analyzer.findHighestClose(records);
        assertEquals(120, highest.getClose(), DELTA);
        assertEquals(LocalDate.of(2025, 1, 5), highest.getDate());
    }

    @Test
    void lowestCloseKeepsTheEarliestDateWhenTied() {
        PriceRecord lowest = analyzer.findLowestClose(records);
        assertEquals(99, lowest.getClose(), DELTA);
        assertEquals(LocalDate.of(2025, 1, 3), lowest.getDate());
    }

    @Test
    void averageClose() {
        assertEquals((100 + 110 + 99 + 99 + 120) / 5.0, analyzer.calculateAverageClose(records), DELTA);
    }

    @Test
    void priceChange() {
        assertEquals(20.0, analyzer.calculatePriceChange(records), DELTA); // 100 -> 120
    }

    @Test
    void averageVolume() {
        assertEquals(3000, analyzer.calculateAverageVolume(records), DELTA);
    }

    @Test
    void dailyChanges() {
        List<DailyChange> changes = analyzer.calculateDailyChanges(records);
        assertEquals(4, changes.size()); // the first day has no previous day
        assertEquals(10.0, changes.get(0).getPercentChange(), DELTA);
        assertEquals(LocalDate.of(2025, 1, 2), changes.get(0).getDate());
    }

    @Test
    void bestAndWorstDay() {
        DailyChange best = analyzer.findBestDay(records);
        assertEquals(LocalDate.of(2025, 1, 5), best.getDate());
        assertEquals(21.0 / 99.0 * 100, best.getPercentChange(), DELTA);

        DailyChange worst = analyzer.findWorstDay(records);
        assertEquals(LocalDate.of(2025, 1, 3), worst.getDate());
        assertEquals(-10.0, worst.getPercentChange(), DELTA);
    }

    @Test
    void emptyPeriodIsAnError() {
        List<PriceRecord> empty = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> analyzer.findHighestClose(empty));
        assertThrows(IllegalArgumentException.class, () -> analyzer.calculateAverageClose(empty));
    }

    @Test
    void bestDayNeedsAtLeastTwoDays() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.findBestDay(makeRecords(100)));
    }

    @Test
    void invalidPricesAreRejected() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> new PriceRecord(date, 1, 1, 1, -5, 100));
        assertThrows(IllegalArgumentException.class, () -> new PriceRecord(date, 1, 1, 1, 1, -100));
    }
}

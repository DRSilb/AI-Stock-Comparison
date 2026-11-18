package market;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class MarketScreenerTest {

    private final MarketScreener screener = new MarketScreener();

    //                                       ticker  days  start  end   return  avg  high  low  avg volume
    private final StockSummary up = new StockSummary("UP", 5, 100, 120, 20.0, 110, 120, 100, 1_000);
    private final StockSummary down = new StockSummary("DOWN", 5, 50, 40, -20.0, 45, 50, 40, 5_000);
    private final StockSummary flat = new StockSummary("FLAT", 5, 300, 300, 0.0, 300, 310, 290, 3_000);

    private final List<StockSummary> all = List.of(up, down, flat);

    /** The tickers of a list, so the expected order is easy to read in the tests. */
    private static List<String> tickers(List<StockSummary> summaries) {
        List<String> result = new ArrayList<>();
        for (StockSummary summary : summaries) {
            result.add(summary.getTicker());
        }
        return result;
    }

    @Test
    void filters() {
        assertEquals(List.of("UP"), tickers(screener.onlyGainers(all)));
        assertEquals(List.of("DOWN"), tickers(screener.onlyLosers(all))); // FLAT is neither
    }

    @Test
    void sorts() {
        assertEquals(List.of("DOWN", "FLAT", "UP"), tickers(screener.sortByTicker(all)));
        assertEquals(List.of("UP", "FLAT", "DOWN"), tickers(screener.sortByReturn(all)));
        assertEquals(List.of("FLAT", "UP", "DOWN"), tickers(screener.sortByPrice(all)));
        assertEquals(List.of("DOWN", "FLAT", "UP"), tickers(screener.sortByVolume(all)));
    }

    @Test
    void theOriginalListIsNotChanged() {
        List<StockSummary> original = new ArrayList<>(all);
        screener.sortByTicker(original);
        screener.onlyGainers(original);
        assertEquals(List.of("UP", "DOWN", "FLAT"), tickers(original));
    }
}

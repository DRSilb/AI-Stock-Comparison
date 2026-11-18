package market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WatchlistTest {

    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final Watchlist watchlist = new Watchlist();

    @BeforeEach
    void captureOutput() {
        Locale.setDefault(Locale.US);
        System.setOut(new PrintStream(output, true)); // Watchlist prints its own messages
    }

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
    }

    /** A stock with one record per day starting 01/01/2025, with the given closing prices. */
    private static Stock makeStock(String ticker, double... closes) {
        List<PriceRecord> records = new ArrayList<>();
        for (int i = 0; i < closes.length; i++) {
            records.add(new PriceRecord(LocalDate.of(2025, 1, 1).plusDays(i),
                    closes[i], closes[i], closes[i], closes[i], 1000));
        }
        return new Stock(ticker, records);
    }

    private static List<String> tickers(List<Stock> stocks) {
        List<String> result = new ArrayList<>();
        for (Stock stock : stocks) {
            result.add(stock.getTicker());
        }
        return result;
    }

    @Test
    void addKeepsTheOrderAndIgnoresDuplicates() {
        watchlist.addStock(makeStock("NVDA", 100));
        watchlist.addStock(makeStock("AAPL", 100));
        watchlist.addStock(makeStock("AAPL", 100)); // already there

        assertEquals(List.of("NVDA", "AAPL"), tickers(watchlist.getStocks()));
        assertTrue(output.toString().contains("Stock is already in watchlist."));
    }

    @Test
    void removeIsNotCaseSensitive() {
        watchlist.addStock(makeStock("NVDA", 100));
        watchlist.addStock(makeStock("AAPL", 100));
        watchlist.removeStock("nvda");
        watchlist.removeStock("TSLA"); // not in the watchlist

        assertEquals(List.of("AAPL"), tickers(watchlist.getStocks()));
        assertTrue(output.toString().contains("Stock not found in watchlist."));
    }

    @Test
    void getStocksReturnsACopy() {
        watchlist.addStock(makeStock("NVDA", 100));
        watchlist.getStocks().clear();      // changing the copy...
        assertEquals(1, watchlist.size());  // ...does not change the watchlist
    }

    @Test
    void displayShowsTheLatestPriceAndChange() {
        watchlist.addStock(makeStock("UP", 100, 110));  // +10%
        watchlist.addStock(makeStock("ONE", 50));       // only 1 day: no change
        watchlist.displayWatchlist();

        String out = output.toString();
        assertTrue(out.contains("UP                        $110.00    +10.00%"), out);
        assertTrue(out.contains("ONE                        $50.00        n/a"), out);
    }
}

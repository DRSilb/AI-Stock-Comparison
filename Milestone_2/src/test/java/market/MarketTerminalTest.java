package market;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs the menu with typed-in answers (instead of a real keyboard) and checks
 * what the program prints.
 */
class MarketTerminalTest {

    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    @BeforeEach
    void captureOutput() {
        Locale.setDefault(Locale.US);
        System.setOut(new PrintStream(output, true));
    }

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
    }

    /** Each argument is one line the "user" types. */
    private String run(String... lines) throws Exception {
        MarketDataManager manager = new MarketDataManager();
        manager.loadData("data");
        Scanner keyboard = new Scanner(String.join("\n", lines) + "\n");
        new MarketTerminal(manager, keyboard).run();
        return output.toString();
    }

    @Test
    void analyzeNvdaFirstQuarter() throws Exception {
        String out = run("3", "NVDA", "01/01/2025", "03/31/2025", "0");

        assertTrue(out.contains("Ticker: NVDA"), out);
        assertTrue(out.contains("Period: 01/02/2025 - 03/31/2025 (60 trading days)"), out);
        assertTrue(out.contains("Highest Closing Price:    $149.43 on 01/06/2025"), out);
        assertTrue(out.contains("Lowest Closing Price:     $106.98 on 03/10/2025"), out);
        assertTrue(out.contains("Average Closing Price:    $126.74"), out);
        assertTrue(out.contains("Price Change:             -21.64%"), out);
        assertTrue(out.contains("Average Trading Volume:   281,460,689"), out);
        assertTrue(out.contains("Best Trading Day:         01/28/2025 (+8.93%)"), out);
        assertTrue(out.contains("Worst Trading Day:        01/27/2025 (-16.97%)"), out);
        assertTrue(out.contains("Goodbye!"), out);
    }

    @Test
    void invalidInputIsRejectedAndAskedAgain() throws Exception {
        String out = run(
                "9",                          // invalid menu option
                "3", "XYZ", "aapl",           // unknown ticker, then a valid one (lower case)
                "13/45/2025", "03/31/2025",   // invalid date, then a valid start date
                "01/01/2025",                 // end date before start date
                "01/01/2026", "03/31/2026",   // a range with no data
                "01/02/2025", "01/02/2025",   // only 1 trading day (analysis needs 2), back to the menu
                "3", "AAPL", "01/01/2025", "12/31/2025", // the whole year
                "0");

        assertTrue(out.contains("Invalid option"), out);
        assertTrue(out.contains("Unknown ticker \"XYZ\""), out);
        assertTrue(out.contains("\"13/45/2025\" is not a valid date"), out);
        assertTrue(out.contains("The start date must not be after the end date"), out);
        assertTrue(out.contains("There is no data between 01/01/2026 and 03/31/2026"), out);
        assertTrue(out.contains("The analysis needs at least 2 trading days"), out);
        assertTrue(out.contains("Ticker: AAPL"), out);
        assertTrue(out.contains("(250 trading days)"), out);
    }

    @Test
    void searchAndHistoricalData() throws Exception {
        String out = run("1", "g", "1", "zzz", "2", "TSLA", "04/01/2025", "04/30/2025", "0");

        assertTrue(out.contains("Found 2 stock(s): AVGO, GOOGL"), out);
        assertTrue(out.contains("No stocks match \"zzz\""), out);
        assertTrue(out.contains("Historical data for TSLA"), out);
        assertTrue(out.contains("04/09/2025     224.69     274.69     223.88     272.20     219,433,373"), out);
        assertTrue(out.contains("21 trading days."), out);
    }

    // ----- Milestone 2: watchlist (options 4, 5, 6) -----

    @Test
    void addViewAndRemoveWatchlistStocks() throws Exception {
        String out = run(
                "4",                  // view: empty
                "5", "aapl",          // add
                "5", "AAPL",          // add again: already there
                "5", "XYZ", "msft",   // unknown ticker, then a valid one
                "5", "nvda",
                "6", "tsla",          // not in the watchlist
                "6", "nvda",          // remove
                "4",                  // view
                "0");

        assertTrue(out.contains("Your watchlist is empty."), out);
        assertTrue(out.contains("AAPL added to watchlist."), out);
        assertTrue(out.contains("Stock is already in watchlist."), out);
        assertTrue(out.contains("Unknown ticker \"XYZ\""), out);
        assertTrue(out.contains("Stock not found in watchlist."), out);
        assertTrue(out.contains("nvda removed from watchlist."), out);
        // latest close 12/31/2025 compared with 12/30/2025: AAPL 273.08 -> 271.86, MSFT 487.48 -> 483.62
        assertTrue(out.contains("AAPL                      $271.86     -0.45%"), out);
        assertTrue(out.contains("MSFT                      $483.62     -0.79%"), out);
        assertFalse(out.contains("NVDA                      $186.50"), out);
    }

    // ----- Milestone 2: Compare Stocks (option 7) -----

    @Test
    void compareThreeStocksForTheWholeYear() throws Exception {
        String out = run("7", "AAPL NVDA MSFT", "01/01/2025", "12/31/2025", "0");

        assertTrue(out.contains("Period: 01/01/2025 - 12/31/2025"), out);
        assertTrue(out.contains("Period Return           +11.49%        +34.84%        +15.54%"), out);
        assertTrue(out.contains("Average Price           $232.24        $153.90        $464.43"), out);
        assertTrue(out.contains("Highest Price           $286.19        $207.04        $542.07"), out);
        assertTrue(out.contains("Lowest Price            $172.42         $94.31        $354.56"), out);
        assertTrue(out.contains("Average Volume       54,172,956    220,762,106     22,194,108"), out);
        assertTrue(out.contains("Best performer:   NVDA (+34.84%)"), out);
        assertTrue(out.contains("Worst performer:  AAPL (+11.49%)"), out);
    }

    @Test
    void compareRejectsBadTickerLists() throws Exception {
        String out = run("7",
                "AAPL",              // only one stock
                "aapl AAPL",         // the same stock twice
                "AAPL XYZ",          // unknown ticker
                "W",                 // the watchlist is empty
                "aapl,msft",         // valid (commas and lower case work)
                "01/01/2026", "01/31/2026",   // no data in 2026
                "01/01/2025", "01/31/2025",
                "0");

        assertTrue(out.contains("Please enter at least 2 different tickers."), out);
        assertTrue(out.contains("Unknown ticker(s): XYZ"), out);
        assertTrue(out.contains("Your watchlist needs at least 2 stocks (it has 0)."), out);
        assertTrue(out.contains("There is no data for AAPL between 01/01/2026 and 01/31/2026"), out);
        assertTrue(out.contains("Trading Days                 20             20"), out);
    }

    @Test
    void compareTheWatchlistByTypingW() throws Exception {
        String out = run("5", "MSFT", "5", "TSLA", "7", "W", "01/01/2025", "12/31/2025", "0");

        assertTrue(out.contains("MSFT           TSLA"), out);
        assertTrue(out.contains("Best performer:   TSLA (+18.57%)"), out);
    }

    // ----- Milestone 2: Sort & Filter (option 8) -----

    @Test
    void gainersBiggestFirst() throws Exception {
        String out = run("8", "1", "0"); // only one question: all stocks, the whole year

        assertTrue(out.contains("Sort & Filter Stocks (all stocks, 01/02/2025 - 12/31/2025)"), out);
        assertTrue(out.contains("GAINERS (01/02/2025 - 12/31/2025)"), out);
        assertTrue(out.contains("1    GOOGL         $313.00    +65.23%       36,259,683"), out);
        assertTrue(out.contains("10   AMZN          $230.82     +4.81%       44,440,810"), out);
    }

    @Test
    void sortByPriceVolumeAndTicker() throws Exception {
        String out = run("8", "3", "8", "4", "8", "5", "0");

        assertTrue(out.contains("BY PRICE"), out);
        assertTrue(out.contains("1    LLY         $1,074.68"), out);   // the highest price
        assertTrue(out.contains("BY VOLUME"), out);
        assertTrue(out.contains("1    NVDA"), out);                    // the highest volume
        assertTrue(out.contains("BY TICKER"), out);
        assertTrue(out.contains("1    AAPL"), out);                    // A to Z
    }

    @Test
    void losersWhenThereAreNoneAndInvalidChoice() throws Exception {
        String out = run("8", "6", "2", "0"); // 6 is not an option; every stock went up in 2025

        assertTrue(out.contains("Please enter a number from 1 to 5."), out);
        assertTrue(out.contains("LOSERS"), out);
        assertTrue(out.contains("No stocks match."), out);
    }
}

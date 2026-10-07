package market;

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
                "01/02/2025", "01/02/2025",   // only 1 trading day (analysis needs 2)
                "", "",                       // Enter twice = full date range
                "0");

        assertTrue(out.contains("Invalid option"), out);
        assertTrue(out.contains("Unknown ticker \"XYZ\""), out);
        assertTrue(out.contains("\"13/45/2025\" is not a valid date"), out);
        assertTrue(out.contains("The start date must not be after the end date"), out);
        assertTrue(out.contains("Found 0 trading day(s) between 01/01/2026 and 03/31/2026"), out);
        assertTrue(out.contains("Found 1 trading day(s)"), out);
        assertTrue(out.contains("Ticker: AAPL"), out);
        assertTrue(out.contains("(250 trading days)"), out);
    }

    @Test
    void searchAndHistoricalData() throws Exception {
        String out = run("1", "g", "1", "zzz", "2", "TSLA", "04/01/2025", "04/30/2025", "0");

        assertTrue(out.contains("2 stock(s) found."), out);
        assertTrue(out.contains("GOOGL"), out);
        assertTrue(out.contains("No stocks match \"zzz\""), out);
        assertTrue(out.contains("Historical data for TSLA"), out);
        assertTrue(out.contains("04/09/2025     224.69     274.69     223.88     272.20     219,433,373"), out);
        assertTrue(out.contains("21 trading days."), out);
    }
}

package market;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Scanner;

/**
 * The text-based user interface: shows the menu, reads what the user types,
 * and prints the results. All calculations are done by MarketAnalyzer.
 */
public class MarketTerminal {

    // Accepts 3/31/2025 or 03/31/2025. STRICT rejects impossible dates like 02/30/2025.
    private static final DateTimeFormatter INPUT_DATE =
            DateTimeFormatter.ofPattern("M/d/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final MarketDataManager dataManager;
    private final MarketAnalyzer analyzer = new MarketAnalyzer();
    private final Scanner scanner;

    public MarketTerminal(MarketDataManager dataManager, Scanner scanner) {
        this.dataManager = dataManager;
        this.scanner = scanner;
    }

    /** Shows the menu until the user chooses Exit. */
    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine("Select an option: ");
            switch (choice) {
                case "1" -> searchStocks();
                case "2" -> viewHistoricalData();
                case "3" -> analyzeStock();
                case "0" -> running = false;
                default -> System.out.println("Invalid option. Please enter 1, 2, 3 or 0.");
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("================================================");
        System.out.println("              MINI MARKET TERMINAL");
        System.out.println("================================================");
        System.out.println("1. Search Stock");
        System.out.println("2. View Historical Data");
        System.out.println("3. Analyze Stock");
        System.out.println("0. Exit");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // Menu option 1: Search Stock
    // ---------------------------------------------------------------

    private void searchStocks() {
        System.out.println();
        System.out.println("--- SEARCH STOCK ---");
        String text = readLine("Enter a ticker or part of one (press Enter to list all): ");
        List<String> matches = dataManager.searchTickers(text);

        if (matches.isEmpty()) {
            System.out.println("No stocks match \"" + text + "\".");
            System.out.println("Available tickers: " + String.join(", ", dataManager.getTickers()));
            return;
        }

        System.out.println();
        System.out.printf("%-8s %-25s %6s %14s%n", "Ticker", "Data Available", "Days", "Latest Close");
        System.out.println("-".repeat(56));
        for (String ticker : matches) {
            Stock stock = dataManager.getStock(ticker);
            String dates = formatDate(stock.getFirstDate()) + " - " + formatDate(stock.getLastDate());
            System.out.printf("%-8s %-25s %6d %14s%n", ticker, dates, stock.getNumberOfRecords(),
                    formatMoney(stock.getLatestRecord().getClose()));
        }
        System.out.println(matches.size() + " stock(s) found.");
    }

    // ---------------------------------------------------------------
    // Menu option 2: View Historical Data
    // ---------------------------------------------------------------

    private void viewHistoricalData() {
        System.out.println();
        System.out.println("--- VIEW HISTORICAL DATA ---");
        Stock stock = askForStock();
        if (stock == null) {
            return; // the user went back to the menu
        }
        List<PriceRecord> records = askForRecordsInDateRange(stock, 1);

        System.out.println();
        System.out.println("Historical data for " + stock.getTicker());
        System.out.printf("%-10s %10s %10s %10s %10s %15s%n", "Date", "Open", "High", "Low", "Close", "Volume");
        System.out.println("-".repeat(70));
        for (PriceRecord record : records) {
            System.out.printf("%-10s %10.2f %10.2f %10.2f %10.2f %,15d%n",
                    formatDate(record.getDate()), record.getOpen(), record.getHigh(),
                    record.getLow(), record.getClose(), record.getVolume());
        }
        System.out.println(records.size() + " trading days.");
    }

    // ---------------------------------------------------------------
    // Menu option 3: Analyze Stock
    // ---------------------------------------------------------------

    private void analyzeStock() {
        System.out.println();
        System.out.println("--- ANALYZE STOCK ---");
        Stock stock = askForStock();
        if (stock == null) {
            return; // the user went back to the menu
        }
        // The best and worst day compare two days, so the period needs at least 2 trading days.
        List<PriceRecord> records = askForRecordsInDateRange(stock, 2);

        PriceRecord highest = analyzer.findHighestClose(records);
        PriceRecord lowest = analyzer.findLowestClose(records);
        double averageClose = analyzer.calculateAverageClose(records);
        double priceChange = analyzer.calculatePriceChange(records);
        double averageVolume = analyzer.calculateAverageVolume(records);
        DailyChange bestDay = analyzer.findBestDay(records);
        DailyChange worstDay = analyzer.findWorstDay(records);

        PriceRecord first = records.get(0);
        PriceRecord last = records.get(records.size() - 1);

        System.out.println();
        System.out.println("Ticker: " + stock.getTicker());
        System.out.println("Period: " + formatDate(first.getDate()) + " - " + formatDate(last.getDate())
                + " (" + records.size() + " trading days)");
        System.out.println();
        System.out.printf("%-26s%s on %s%n", "Highest Closing Price:", formatMoney(highest.getClose()), formatDate(highest.getDate()));
        System.out.printf("%-26s%s on %s%n", "Lowest Closing Price:", formatMoney(lowest.getClose()), formatDate(lowest.getDate()));
        System.out.printf("%-26s%s%n", "Average Closing Price:", formatMoney(averageClose));
        System.out.printf("%-26s%s%n", "Price Change:", formatPercent(priceChange));
        System.out.printf("%-26s%,.0f%n", "Average Trading Volume:", averageVolume);
        System.out.println();
        System.out.printf("%-26s%s (%s)%n", "Best Trading Day:", formatDate(bestDay.getDate()), formatPercent(bestDay.getPercentChange()));
        System.out.printf("%-26s%s (%s)%n", "Worst Trading Day:", formatDate(worstDay.getDate()), formatPercent(worstDay.getPercentChange()));
    }

    // ---------------------------------------------------------------
    // Helpers for reading input
    // ---------------------------------------------------------------

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /** Asks for a ticker until a valid one is entered. Returns null if the user just presses Enter. */
    private Stock askForStock() {
        while (true) {
            String ticker = readLine("Enter a ticker symbol (or press Enter to go back): ");
            if (ticker.isEmpty()) {
                return null;
            }
            if (dataManager.hasStock(ticker)) {
                return dataManager.getStock(ticker);
            }
            System.out.println("Unknown ticker \"" + ticker + "\". Available tickers: "
                    + String.join(", ", dataManager.getTickers()));
        }
    }

    /**
     * Asks for a start and end date until the range is valid and contains at least
     * minimumDays trading days. Returns the price records in that range.
     */
    private List<PriceRecord> askForRecordsInDateRange(Stock stock, int minimumDays) {
        LocalDate firstDate = stock.getFirstDate();
        LocalDate lastDate = stock.getLastDate();
        System.out.println("Data is available from " + formatDate(firstDate) + " to " + formatDate(lastDate) + ".");

        while (true) {
            LocalDate start = askForDate("Start date (MM/DD/YYYY, press Enter for " + formatDate(firstDate) + "): ", firstDate);
            LocalDate end = askForDate("End date   (MM/DD/YYYY, press Enter for " + formatDate(lastDate) + "): ", lastDate);

            if (start.isAfter(end)) {
                System.out.println("The start date must not be after the end date. Please try again.");
                continue;
            }
            List<PriceRecord> records = stock.getRecordsBetween(start, end);
            if (records.size() < minimumDays) {
                System.out.println("Found " + records.size() + " trading day(s) between " + formatDate(start)
                        + " and " + formatDate(end) + ", but at least " + minimumDays + " are needed. Please try again.");
                continue;
            }
            return records;
        }
    }

    /** Asks for one date until a valid date is entered. Pressing Enter returns defaultDate. */
    private LocalDate askForDate(String prompt, LocalDate defaultDate) {
        while (true) {
            String input = readLine(prompt);
            if (input.isEmpty()) {
                return defaultDate;
            }
            try {
                return LocalDate.parse(input, INPUT_DATE);
            } catch (DateTimeParseException e) {
                System.out.println("\"" + input + "\" is not a valid date. Use MM/DD/YYYY, for example 03/31/2025.");
            }
        }
    }

    // ---------------------------------------------------------------
    // Helpers for formatting output
    // ---------------------------------------------------------------

    private String formatDate(LocalDate date) {
        return date.format(DISPLAY_DATE);
    }

    private String formatMoney(double amount) {
        return String.format("$%,.2f", amount);
    }

    /** Always shows the sign, e.g. +4.25% or -1.10% */
    private String formatPercent(double percent) {
        return String.format("%+.2f%%", percent);
    }
}

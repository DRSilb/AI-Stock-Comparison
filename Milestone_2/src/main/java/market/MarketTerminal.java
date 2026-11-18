package market;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;


/**
 * this is the user interface where the user can search, analyze and check each stock price.
 * it uses the market analyzer to print out the data.
 * Milestone 2 added the watchlist (options 4-6), comparing stocks (7) and sorting & filtering (8).
 * the calculations are done by MarketAnalyzer and the sorting/filtering by MarketScreener.
 */
public class MarketTerminal {

    // Dates are typed and printed as MM/DD/YYYY. STRICT rejects impossible dates like 02/30/2025.
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MM/dd/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final MarketDataManager dataManager;
    private final MarketAnalyzer analyzer = new MarketAnalyzer();
    private final MarketScreener screener = new MarketScreener();
    private final Scanner scanner;
    private final Watchlist watchlist = new Watchlist();
    public MarketTerminal(MarketDataManager dataManager, Scanner scanner) {
        this.dataManager = dataManager;
        this.scanner = scanner;
    }

    /** continues to show the menu until the user decides to exit the terminal */
    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine("Select an option: ");
            switch (choice) {
                case "1" -> searchStock();
                case "2" -> viewHistoricalData();
                case "3" -> analyzeStock();
                case "4" -> watchlist.displayWatchlist();
                case "5" -> {
                    Stock stock = askForStock();
                    watchlist.addStock(stock);
                }
                case "6" -> {
                    String ticker = readLine("Enter ticker to remove: ");
                    watchlist.removeStock(ticker);}
                case "7" -> compareStocks();
                case "8" -> sortAndFilterStocks();
                case "0" -> running = false;
                default -> System.out.println("Invalid option. Please enter a number from 0 to 8.");
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Market Terminal");
        System.out.println("---------------");
        System.out.println("1. Search Stock");
        System.out.println("2. View Historical Data");
        System.out.println("3. Analyze Stock");
        System.out.println("4. View Watchlist");
        System.out.println("5. Add Stock to Watchlist");
        System.out.println("6. Remove Stock from Watchlist");
        System.out.println("7. Compare Stocks");
        System.out.println("8. Sort & Filter Stocks");
        System.out.println("0. Exit");
        System.out.println();
    }

    /** this is the menu option 1: searches for the stock */
    private void searchStock() {
        String text = readLine("Enter a ticker or part of one (press Enter to list all): ");
        List<String> matches = dataManager.searchTickers(text);

        if (matches.isEmpty()) {
            System.out.println("No stocks match \"" + text + "\".");
            System.out.println("Available tickers: " + String.join(", ", dataManager.getTickers()));
        } else {
            System.out.println("Found " + matches.size() + " stock(s): " + String.join(", ", matches));
        }
    }

    /** Menu option 2: prints every trading day of a stock in a date range. */
    private void viewHistoricalData() {
        Stock stock = askForStock();
        List<PriceRecord> records = askForDateRange(stock);

        System.out.println();
        System.out.println("Historical data for " + stock.getTicker());
        System.out.printf("%-10s %10s %10s %10s %10s %15s%n", "Date", "Open", "High", "Low", "Close", "Volume");
        for (PriceRecord record : records) {
            System.out.printf("%-10s %10.2f %10.2f %10.2f %10.2f %,15d%n",
                    record.getDate().format(DATE_FORMAT), record.getOpen(), record.getHigh(),
                    record.getLow(), record.getClose(), record.getVolume());
        }
        System.out.println(records.size() + " trading days.");
    }

    /** Menu option 3: prints the analysis of a stock over a date range. */
    private void analyzeStock() {
        Stock stock = askForStock();
        List<PriceRecord> records = askForDateRange(stock);

        // The best and worst day compare each day with the day before it, so 2 days are needed.
        if (records.size() < 2) {
            System.out.println("The analysis needs at least 2 trading days. Please choose a longer period.");
            return;
        }

        PriceRecord highest = analyzer.findHighestClose(records);
        PriceRecord lowest = analyzer.findLowestClose(records);
        DailyChange bestDay = analyzer.findBestDay(records);
        DailyChange worstDay = analyzer.findWorstDay(records);
        LocalDate firstDate = records.get(0).getDate();
        LocalDate lastDate = records.get(records.size() - 1).getDate();

        System.out.println();
        System.out.println("Ticker: " + stock.getTicker());
        System.out.println("Period: " + firstDate.format(DATE_FORMAT) + " - " + lastDate.format(DATE_FORMAT)
                + " (" + records.size() + " trading days)");
        System.out.println();
        System.out.printf("Highest Closing Price:    $%,.2f on %s%n", highest.getClose(), highest.getDate().format(DATE_FORMAT));
        System.out.printf("Lowest Closing Price:     $%,.2f on %s%n", lowest.getClose(), lowest.getDate().format(DATE_FORMAT));
        System.out.printf("Average Closing Price:    $%,.2f%n", analyzer.calculateAverageClose(records));
        System.out.printf("Price Change:             %+.2f%%%n", analyzer.calculatePriceChange(records));
        System.out.printf("Average Trading Volume:   %,.0f%n", analyzer.calculateAverageVolume(records));
        System.out.println();
        System.out.printf("Best Trading Day:         %s (%+.2f%%)%n", bestDay.getDate().format(DATE_FORMAT), bestDay.getPercentChange());
        System.out.printf("Worst Trading Day:        %s (%+.2f%%)%n", worstDay.getDate().format(DATE_FORMAT), worstDay.getPercentChange());
    }

    /** Menu option 7: compares 2 or more stocks side by side over the same period. */
    private void compareStocks() {
        List<Stock> stocks = askForStocks();
        DateRange range = askForPeriod(stocks);

        List<StockSummary> summaries = new ArrayList<>();
        for (Stock stock : stocks) {
            summaries.add(analyzer.summarize(stock, range));
        }

        System.out.println();
        System.out.println("STOCK COMPARISON");
        System.out.println("Period: " + range.getStart().format(DATE_FORMAT) + " - " + range.getEnd().format(DATE_FORMAT));
        System.out.println();

        // header row: one column per stock
        StringBuilder header = new StringBuilder(String.format("%-16s", ""));
        for (StockSummary summary : summaries) {
            header.append(String.format("%15s", summary.getTicker()));
        }
        System.out.println(header);
        System.out.println("-".repeat(header.length()));

        printComparisonRow("Period Return", summaries, s -> percent(s.getPeriodReturn()));
        printComparisonRow("Start Price", summaries, s -> money(s.getStartPrice()));
        printComparisonRow("End Price", summaries, s -> money(s.getEndPrice()));
        printComparisonRow("Average Price", summaries, s -> money(s.getAveragePrice()));
        printComparisonRow("Highest Price", summaries, s -> money(s.getHighestPrice()));
        printComparisonRow("Lowest Price", summaries, s -> money(s.getLowestPrice()));
        printComparisonRow("Average Volume", summaries, s -> volume(s.getAverageVolume()));
        printComparisonRow("Trading Days", summaries, s -> String.valueOf(s.getTradingDays()));

        // after sorting by return, the first stock did best and the last did worst
        List<StockSummary> byReturn = screener.sortByReturn(summaries);
        StockSummary best = byReturn.get(0);
        StockSummary worst = byReturn.get(byReturn.size() - 1);
        System.out.println();
        System.out.println("Best performer:   " + best.getTicker() + " (" + percent(best.getPeriodReturn()) + ")");
        System.out.println("Worst performer:  " + worst.getTicker() + " (" + percent(worst.getPeriodReturn()) + ")");
    }

    /**
     * Prints one row of the comparison table. The "value" lambda picks the number
     * to show for each stock, for example s -> money(s.getEndPrice()).
     */
    private void printComparisonRow(String label, List<StockSummary> summaries, Function<StockSummary, String> value) {
        StringBuilder row = new StringBuilder(String.format("%-16s", label));
        for (StockSummary summary : summaries) {
            row.append(String.format("%15s", value.apply(summary)));
        }
        System.out.println(row);
    }

    /**
     * Menu option 8: one quick list of stocks, sorted or filtered.
     * To keep it short it asks only one question, and assumes the rest:
     * it always uses all stocks and the full period in the data.
     */
    private void sortAndFilterStocks() {
        List<Stock> stocks = new ArrayList<>();
        for (String ticker : dataManager.getTickers()) {
            stocks.add(dataManager.getStock(ticker));
        }
        DateRange range = fullPeriod(stocks);

        List<StockSummary> summaries = new ArrayList<>();
        for (Stock stock : stocks) {
            summaries.add(analyzer.summarize(stock, range));
        }

        String period = range.getStart().format(DATE_FORMAT) + " - " + range.getEnd().format(DATE_FORMAT);
        System.out.println();
        System.out.println("Sort & Filter Stocks (all stocks, " + period + ")");
        System.out.println("1. Gainers (biggest gain first)");
        System.out.println("2. Losers (biggest loss first)");
        System.out.println("3. Price (highest first)");
        System.out.println("4. Volume (highest first)");
        System.out.println("5. Ticker (A to Z)");

        String title;
        switch (askForChoice("Select an option: ", 1, 5)) {
            case 1 -> {
                summaries = screener.sortByReturn(screener.onlyGainers(summaries));
                title = "GAINERS";
            }
            case 2 -> {
                summaries = screener.sortByReturn(screener.onlyLosers(summaries));
                Collections.reverse(summaries); // most negative return first
                title = "LOSERS";
            }
            case 3 -> {
                summaries = screener.sortByPrice(summaries);
                title = "BY PRICE";
            }
            case 4 -> {
                summaries = screener.sortByVolume(summaries);
                title = "BY VOLUME";
            }
            default -> {
                summaries = screener.sortByTicker(summaries);
                title = "BY TICKER";
            }
        }

        System.out.println();
        System.out.println(title + " (" + period + ")");
        System.out.println();
        if (summaries.isEmpty()) {
            System.out.println("No stocks match.");
            return;
        }
        System.out.printf("%-4s %-8s %12s %10s %16s%n", "#", "Ticker", "End Price", "Return", "Avg Volume");
        System.out.println("-".repeat(54));
        int rank = 1;
        for (StockSummary summary : summaries) {
            System.out.printf("%-4d %-8s %12s %10s %16s%n", rank, summary.getTicker(),
                    money(summary.getEndPrice()), percent(summary.getPeriodReturn()),
                    volume(summary.getAverageVolume()));
            rank++;
        }
    }

    /** From the earliest to the latest date that any of the stocks has data for. */
    private DateRange fullPeriod(List<Stock> stocks) {
        LocalDate first = stocks.get(0).getFirstDate();
        LocalDate last = stocks.get(0).getLastDate();
        for (Stock stock : stocks) {
            if (stock.getFirstDate().isBefore(first)) {
                first = stock.getFirstDate();
            }
            if (stock.getLastDate().isAfter(last)) {
                last = stock.getLastDate();
            }
        }
        return new DateRange(first, last);
    }

    /** Prints the prompt and returns the line the user types. */
    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /** Asks for a ticker until the user types one that exists. */
    private Stock askForStock() {
        while (true) {
            String ticker = readLine("Enter a ticker symbol: ");
            Stock stock = dataManager.getStock(ticker);
            if (stock != null) {
                return stock;
            }
            System.out.println("Unknown ticker \"" + ticker + "\". Available tickers: "
                    + String.join(", ", dataManager.getTickers()));
        }
    }

    /** Asks for a start and end date until the range is valid and has data. Returns the records in it. */
    private List<PriceRecord> askForDateRange(Stock stock) {
        DateRange range = askForPeriod(List.of(stock));
        return stock.getRecordsIn(range);
    }

    /**
     * Asks for a start and end date until the period is valid and EVERY one of
     * the stocks has data in it. (Milestone 2: askForDateRange only worked for one stock.)
     */
    private DateRange askForPeriod(List<Stock> stocks) {
        // the earliest and latest date any of the stocks has data for
        LocalDate first = stocks.get(0).getFirstDate();
        LocalDate last = stocks.get(0).getLastDate();
        for (Stock stock : stocks) {
            if (stock.getFirstDate().isBefore(first)) {
                first = stock.getFirstDate();
            }
            if (stock.getLastDate().isAfter(last)) {
                last = stock.getLastDate();
            }
        }
        System.out.println("Data is available from " + first.format(DATE_FORMAT)
                + " to " + last.format(DATE_FORMAT) + ".");

        while (true) {
            LocalDate start = askForDate("Start date (MM/DD/YYYY): ");
            LocalDate end = askForDate("End date   (MM/DD/YYYY): ");

            if (start.isAfter(end)) {
                System.out.println("The start date must not be after the end date. Please try again.");
                continue;
            }
            DateRange range = new DateRange(start, end);

            Stock stockWithoutData = null;
            for (Stock stock : stocks) {
                if (stock.getRecordsIn(range).isEmpty()) {
                    stockWithoutData = stock;
                    break;
                }
            }
            if (stockWithoutData != null) {
                String forTicker = stocks.size() > 1 ? " for " + stockWithoutData.getTicker() : "";
                System.out.println("There is no data" + forTicker + " between " + start.format(DATE_FORMAT)
                        + " and " + end.format(DATE_FORMAT) + ". Please try again.");
                continue;
            }
            return range;
        }
    }

    /** Asks for a date until the user types a valid one. */
    private LocalDate askForDate(String prompt) {
        while (true) {
            String input = readLine(prompt);
            try {
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("\"" + input + "\" is not a valid date. Use MM/DD/YYYY, for example 03/31/2025.");
            }
        }
    }

    // ----- input helpers added in Milestone 2 -----

    /**
     * Asks for 2 or more different tickers until all of them exist.
     * Typing W compares the stocks in the watchlist instead.
     */
    private List<Stock> askForStocks() {
        while (true) {
            String text = readLine("Enter 2 or more tickers separated by spaces (or W for your watchlist): ");

            if (text.equalsIgnoreCase("W")) {
                if (watchlist.size() >= 2) {
                    return watchlist.getStocks();
                }
                System.out.println("Your watchlist needs at least 2 stocks (it has " + watchlist.size() + ").");
                continue;
            }

            // a Set ignores duplicates, so "AAPL aapl" counts as one stock
            Set<String> tickers = new LinkedHashSet<>();
            for (String part : text.split("[\\s,]+")) {
                if (!part.isEmpty()) {
                    tickers.add(part.toUpperCase());
                }
            }

            List<Stock> stocks = new ArrayList<>();
            List<String> unknown = new ArrayList<>();
            for (String ticker : tickers) {
                Stock stock = dataManager.getStock(ticker);
                if (stock == null) {
                    unknown.add(ticker);
                } else {
                    stocks.add(stock);
                }
            }

            if (!unknown.isEmpty()) {
                System.out.println("Unknown ticker(s): " + String.join(", ", unknown)
                        + ". Available tickers: " + String.join(", ", dataManager.getTickers()));
            } else if (stocks.size() < 2) {
                System.out.println("Please enter at least 2 different tickers.");
            } else {
                return stocks;
            }
        }
    }

    /** Asks for a whole number from min to max (for example a menu choice). */
    private int askForChoice(String prompt, int min, int max) {
        while (true) {
            String input = readLine(prompt);
            try {
                int choice = Integer.parseInt(input);
                if (choice >= min && choice <= max) {
                    return choice;
                }
            } catch (NumberFormatException e) {
                // not a number: the message below is printed
            }
            System.out.println("Please enter a number from " + min + " to " + max + ".");
        }
    }

    // ----- formatting helpers -----

    /** 1234.5 -> "$1,234.50" */
    private static String money(double amount) {
        return String.format("$%,.2f", amount);
    }

    /** 2.5 -> "+2.50%" */
    private static String percent(double percent) {
        return String.format("%+.2f%%", percent);
    }

    /** 281460689.4 -> "281,460,689" */
    private static String volume(double volume) {
        return String.format("%,.0f", volume);
    }
}

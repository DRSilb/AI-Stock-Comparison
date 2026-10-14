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

    // Dates are typed and printed as MM/DD/YYYY. STRICT rejects impossible dates like 02/30/2025.
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MM/dd/uuuu").withResolverStyle(ResolverStyle.STRICT);

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
                case "1" -> searchStock();
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

    /** Menu option 1: lists the tickers that contain what the user types. */
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
        System.out.println("Data is available from " + stock.getFirstDate().format(DATE_FORMAT)
                + " to " + stock.getLastDate().format(DATE_FORMAT) + ".");
        while (true) {
            LocalDate start = askForDate("Start date (MM/DD/YYYY): ");
            LocalDate end = askForDate("End date   (MM/DD/YYYY): ");

            if (start.isAfter(end)) {
                System.out.println("The start date must not be after the end date. Please try again.");
                continue;
            }
            List<PriceRecord> records = stock.getRecordsBetween(start, end);
            if (records.isEmpty()) {
                System.out.println("There is no data between " + start.format(DATE_FORMAT)
                        + " and " + end.format(DATE_FORMAT) + ". Please try again.");
                continue;
            }
            return records;
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
}

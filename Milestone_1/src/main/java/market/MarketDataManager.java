package market;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Loads the stock data from the CSV files and keeps it organized by ticker symbol.
 *
 * Each file in the data folder holds one stock. The ticker comes from the file
 * name: "NVDA_2025.csv" becomes "NVDA".
 */
public class MarketDataManager {

    private static final String EXPECTED_HEADER = "Date,Open,High,Low,Close,Volume";

    // A TreeMap keeps the tickers in alphabetical order.
    private final Map<String, Stock> stocks = new TreeMap<>();

    // Problems found while loading (bad rows, unreadable files) to show the user.
    private final List<String> warnings = new ArrayList<>();

    /**
     * Reads every .csv file in the folder.
     *
     * @throws FileNotFoundException if the folder does not exist or has no usable .csv files
     */
    public void loadData(String folderPath) throws FileNotFoundException {
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            throw new FileNotFoundException("Data folder not found: " + folder.getAbsolutePath());
        }

        File[] files = folder.listFiles();
        if (files != null) {
            Arrays.sort(files); // load in alphabetical order
            for (File file : files) {
                String name = file.getName();
                // Skip hidden files such as "._AAPL_2025.csv" (created by macOS)
                if (name.toLowerCase().endsWith(".csv") && !name.startsWith(".")) {
                    loadFile(file);
                }
            }
        }

        if (stocks.isEmpty()) {
            throw new FileNotFoundException("No stock data files (.csv) found in " + folder.getAbsolutePath());
        }
    }

    /** Reads one CSV file and adds the stock to the map. Problems are added to the warnings list. */
    private void loadFile(File file) {
        String fileName = file.getName();
        String ticker = tickerFromFileName(fileName);
        if (stocks.containsKey(ticker)) {
            warnings.add(fileName + ": skipped, data for " + ticker + " was already loaded from another file");
            return;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            warnings.add(fileName + ": could not be read");
            return;
        }

        if (lines.isEmpty() || !lines.get(0).trim().equalsIgnoreCase(EXPECTED_HEADER)) {
            warnings.add(fileName + ": skipped, the first line must be " + EXPECTED_HEADER);
            return;
        }

        List<PriceRecord> records = new ArrayList<>();
        int badRows = 0;
        for (int i = 1; i < lines.size(); i++) { // start at 1 to skip the header line
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                records.add(parseLine(line));
            } catch (IllegalArgumentException | DateTimeParseException e) {
                badRows++; // NumberFormatException is a kind of IllegalArgumentException
            }
        }

        if (badRows > 0) {
            warnings.add(fileName + ": skipped " + badRows + " invalid row(s)");
        }
        if (records.isEmpty()) {
            warnings.add(fileName + ": skipped, no valid rows");
            return;
        }
        stocks.put(ticker, new Stock(ticker, records));
    }

    /** Turns one line like "2025-01-02,248.93,249.1,241.82,243.85,55740731" into a PriceRecord. */
    private PriceRecord parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Expected 6 values but found " + parts.length);
        }
        LocalDate date = LocalDate.parse(parts[0].trim()); // format: 2025-01-02
        double open = Double.parseDouble(parts[1].trim());
        double high = Double.parseDouble(parts[2].trim());
        double low = Double.parseDouble(parts[3].trim());
        double close = Double.parseDouble(parts[4].trim());
        long volume = Long.parseLong(parts[5].trim());
        return new PriceRecord(date, open, high, low, close, volume);
    }

    /** "NVDA_2025.csv" -> "NVDA", "msft.csv" -> "MSFT" */
    private String tickerFromFileName(String fileName) {
        String name = fileName.substring(0, fileName.length() - ".csv".length());
        int underscore = name.indexOf('_');
        if (underscore >= 0) {
            name = name.substring(0, underscore);
        }
        return name.toUpperCase();
    }

    public boolean hasStock(String ticker) {
        return stocks.containsKey(ticker.trim().toUpperCase());
    }

    /** Returns the stock with this ticker (not case-sensitive), or null if there is none. */
    public Stock getStock(String ticker) {
        return stocks.get(ticker.trim().toUpperCase());
    }

    /** All tickers in alphabetical order. */
    public List<String> getTickers() {
        return new ArrayList<>(stocks.keySet());
    }

    /** Tickers that contain the search text, e.g. "G" finds AVGO and GOOGL. */
    public List<String> searchTickers(String text) {
        String search = text.trim().toUpperCase();
        List<String> matches = new ArrayList<>();
        for (String ticker : stocks.keySet()) {
            if (ticker.contains(search)) {
                matches.add(ticker);
            }
        }
        return matches;
    }

    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
}

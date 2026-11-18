package market;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * loads all of the stocks and data into the manager
 * changes the file names to be more straight forward: from NVDA_2025.csv to NVDA
 */
public class MarketDataManager {

    // Every loaded stock, found by its ticker (for example "NVDA" -> the NVDA Stock).
    // the treeMap keeps the tickers in alphabetical order

    private final Map<String, Stock> stocks = new TreeMap<>();

    /**
     * Loads every .csv file in the folder.
     * @throws FileNotFoundException if the folder does not exist or has no usable .csv files
     */
    public void loadData(String folderPath) throws FileNotFoundException {
        // listFiles() returns null when the folder does not exist.
        File[] files = new File(folderPath).listFiles();
        if (files == null) {
            throw new FileNotFoundException("Data folder not found: " + folderPath);
        }

        for (File file : files) {
            String name = file.getName();
            // Only read .csv files. Names that start with "." are hidden files
            // (macOS adds files like "._AAPL_2025.csv" to zip files), so skip them.
            if (name.endsWith(".csv") && !name.startsWith(".")) {
                loadFile(file);
            }
        }

        if (stocks.isEmpty()) {
            throw new FileNotFoundException("No stock data (.csv files) found in: " + folderPath);
        }
    }

    /** Reads one csv file and stores it as a Stock. if the row cannot be read, its skipped */
    private void loadFile(File file) throws FileNotFoundException {
        // "NVDA_2025.csv" -> "NVDA"
        String ticker = file.getName().split("_")[0].replace(".csv", "").toUpperCase();

        List<PriceRecord> records = new ArrayList<>();
        int badRows = 0;

        Scanner reader = new Scanner(file);
        if (reader.hasNextLine()) {
            reader.nextLine(); // skip the header line: Date,Open,High,Low,Close,Volume
        }
        while (reader.hasNextLine()) {
            String line = reader.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                records.add(parseLine(line));
            } catch (IllegalArgumentException | DateTimeParseException e) {
                badRows++;
            }
        }
        reader.close();

        if (records.isEmpty()) {
            System.out.println("Warning: " + file.getName() + " has no valid rows, so it was skipped.");
            return;
        }
        if (badRows > 0) {
            System.out.println("Warning: skipped " + badRows + " invalid row(s) in " + file.getName() + ".");
        }
        stocks.put(ticker, new Stock(ticker, records));
    }

    /**
     * Turns one row of a CSV file into a PriceRecord, for example:
     * "2025-01-02,136.0,138.88,134.63,138.31,198247166"
     * @throws IllegalArgumentException or DateTimeParseException if the row is not valid
     */
    private PriceRecord parseLine(String line) {
        String[] values = line.split(",");
        if (values.length != 6) {
            throw new IllegalArgumentException("Expected 6 values but found " + values.length);
        }
        LocalDate date = LocalDate.parse(values[0].trim()); // the files use the format 2025-01-02
        double open = Double.parseDouble(values[1].trim());
        double high = Double.parseDouble(values[2].trim());
        double low = Double.parseDouble(values[3].trim());
        double close = Double.parseDouble(values[4].trim());
        long volume = Long.parseLong(values[5].trim());
        return new PriceRecord(date, open, high, low, close, volume);
    }

    /** returns the stock with this ticker, or null if there is none. nvda and NVDA both work here */
    public Stock getStock(String ticker) {
        return stocks.get(ticker.trim().toUpperCase());
    }

    /** All tickers in alphabetical order. */
    public List<String> getTickers() {
        return new ArrayList<>(stocks.keySet());
    }

    /** Tickers that contain the search text, e.g. "G" finds AVGO and GOOGL. Empty text finds all of them. */
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
}

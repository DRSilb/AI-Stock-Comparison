package market;
import java.util.ArrayList;
import java.util.List;

/**
 * the user's personal watchlist: the stocks they want to keep an eye on.
 * the user can add a stock, remove a stock, and see the latest price of every stock in it.
 */
public class Watchlist {
    private ArrayList<Stock> stocks;
    private final MarketAnalyzer analyzer = new MarketAnalyzer(); // used for the "Change" column

    public Watchlist() {
        stocks = new ArrayList<>();
    }
    //This is the method to add a stock to the watchlist
    public void addStock(Stock stock) {
        for (Stock s : stocks) {
            if (s.getTicker().equals(stock.getTicker())) {
                System.out.println("Stock is already in watchlist.");
                return;
            }
        }
        stocks.add(stock);
        System.out.println(stock.getTicker() + " added to watchlist.");
    }
    //This is the method to remove a stock from the watchlist
    public void removeStock(String ticker) {
        for (Stock stock : stocks) {
            if (stock.getTicker().equalsIgnoreCase(ticker)) {
                stocks.remove(stock);
                System.out.println(ticker + " removed from watchlist.");
                return;
            }
        }
        System.out.println("Stock not found in watchlist.");
    }
    //This is the method to display the entire watchlist
    //Milestone 2: shows a table with the latest available price and the change from the day before
    public void displayWatchlist() {
        if (stocks.isEmpty()) {
            System.out.println("Your watchlist is empty.");
            return;
        }
        System.out.println();
        System.out.println("MY WATCHLIST");
        System.out.println();
        System.out.printf("%-8s %24s %10s%n", "Ticker", "Latest Available Price", "Change");
        System.out.println("-".repeat(44));
        for (Stock stock : stocks) {
            PriceRecord latest = stock.getLatestRecord();

            // a stock with only 1 day of data has no day before it to compare with
            String change = "n/a";
            if (stock.getNumberOfRecords() >= 2) {
                double percent = analyzer.findLatestChange(stock.getRecords()).getPercentChange();
                change = String.format("%+.2f%%", percent);
            }
            System.out.printf("%-8s %24s %10s%n",
                    stock.getTicker(), String.format("$%,.2f", latest.getClose()), change);
        }
        System.out.println();
        System.out.println("Latest Available Price = the most recent closing price in the data.");
        System.out.println("Change = change from the closing price of the trading day before.");
    }

    //Milestone 2: the stocks in the watchlist, so they can be compared or sorted.
    //returns a copy, so the watchlist can only be changed with addStock and removeStock
    public List<Stock> getStocks() {
        return new ArrayList<>(stocks);
    }

    public boolean isEmpty() {
        return stocks.isEmpty();
    }

    public int size() {
        return stocks.size();
    }
}

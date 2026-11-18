package market;
import java.util.ArrayList;

public class Watchlist {
    private ArrayList<Stock> stocks;
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
    public void displayWatchlist() {
        if (stocks.isEmpty()) {
            System.out.println("Your watchlist is empty.");
            return;
        }
        System.out.println("MY WATCHLIST");
        for (Stock stock : stocks) {
            PriceRecord latest = stock.getLatestRecord();

            System.out.println(stock.getTicker()
                    + " - $" + latest.getClose());
        }
    }
}

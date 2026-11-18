package market;

/**
 * The main numbers for one stock over one period.
 *
 * The Compare Stocks table shows one StockSummary per column, and the
 * Sort & Filter screen sorts and filters a list of them.
 * MarketAnalyzer.summarize() does the calculations and creates the object;
 * this class only stores the results, so it cannot be changed afterwards.
 */
public class StockSummary {

    private final String ticker;
    private final int tradingDays;
    private final double startPrice;    // first closing price in the period
    private final double endPrice;      // last closing price in the period
    private final double periodReturn;  // % change from startPrice to endPrice
    private final double averagePrice;  // average closing price
    private final double highestPrice;  // highest closing price
    private final double lowestPrice;   // lowest closing price
    private final double averageVolume;

    public StockSummary(String ticker, int tradingDays, double startPrice, double endPrice,
                        double periodReturn, double averagePrice, double highestPrice,
                        double lowestPrice, double averageVolume) {
        this.ticker = ticker;
        this.tradingDays = tradingDays;
        this.startPrice = startPrice;
        this.endPrice = endPrice;
        this.periodReturn = periodReturn;
        this.averagePrice = averagePrice;
        this.highestPrice = highestPrice;
        this.lowestPrice = lowestPrice;
        this.averageVolume = averageVolume;
    }

    public String getTicker() {
        return ticker;
    }

    public int getTradingDays() {
        return tradingDays;
    }

    public double getStartPrice() {
        return startPrice;
    }

    /** The last closing price in the period (the "latest" price for that period). */
    public double getEndPrice() {
        return endPrice;
    }

    /** For example 12.5 means the price went up 12.5% over the period. */
    public double getPeriodReturn() {
        return periodReturn;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public double getHighestPrice() {
        return highestPrice;
    }

    public double getLowestPrice() {
        return lowestPrice;
    }

    public double getAverageVolume() {
        return averageVolume;
    }
}

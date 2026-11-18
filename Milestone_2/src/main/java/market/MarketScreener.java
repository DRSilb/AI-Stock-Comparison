package market;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sorts and filters stocks for the "Sort & Filter Stocks" menu option.
 *
 * Every method takes a list of StockSummary objects (one per stock, all for the
 * same period) and returns a NEW list. The list that was passed in is never changed.
 * Like MarketAnalyzer, this class does not print anything.
 */
public class MarketScreener {

    // ----- Filtering (uses streams and lambdas) -----

    /** Only the stocks whose price went up during the period. */
    public List<StockSummary> onlyGainers(List<StockSummary> summaries) {
        return summaries.stream()
                .filter(summary -> summary.getPeriodReturn() > 0)
                .collect(Collectors.toList());
    }

    /** Only the stocks whose price went down during the period. */
    public List<StockSummary> onlyLosers(List<StockSummary> summaries) {
        return summaries.stream()
                .filter(summary -> summary.getPeriodReturn() < 0)
                .collect(Collectors.toList());
    }


    // ----- Sorting (uses Comparators) -----

    /** Alphabetical order by ticker symbol: A to Z. */
    public List<StockSummary> sortByTicker(List<StockSummary> summaries) {
        return sorted(summaries, Comparator.comparing(StockSummary::getTicker));
    }

    /** Highest period return (% change) first. */
    public List<StockSummary> sortByReturn(List<StockSummary> summaries) {
        return sorted(summaries, Comparator.comparingDouble(StockSummary::getPeriodReturn).reversed());
    }

    /** Highest price at the end of the period first. */
    public List<StockSummary> sortByPrice(List<StockSummary> summaries) {
        return sorted(summaries, Comparator.comparingDouble(StockSummary::getEndPrice).reversed());
    }

    /** Highest average daily trading volume first. */
    public List<StockSummary> sortByVolume(List<StockSummary> summaries) {
        return sorted(summaries, Comparator.comparingDouble(StockSummary::getAverageVolume).reversed());
    }

    /** Copies the list, then sorts the copy in the given order. */
    private List<StockSummary> sorted(List<StockSummary> summaries, Comparator<StockSummary> order) {
        List<StockSummary> result = new ArrayList<>(summaries);
        result.sort(order);
        return result;
    }
}

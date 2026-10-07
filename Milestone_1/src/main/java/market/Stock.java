package market;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A stock: its ticker symbol and its daily price history.
 */
public class Stock {

    private final String ticker;
    private final List<PriceRecord> records; // always sorted by date, oldest first

    public Stock(String ticker, List<PriceRecord> records) {
        if (records.isEmpty()) {
            throw new IllegalArgumentException("A stock needs at least one price record");
        }
        this.ticker = ticker.toUpperCase();
        this.records = new ArrayList<>(records); // copy, so outside code cannot change our list
        Collections.sort(this.records);          // uses PriceRecord.compareTo (by date)
    }

    public String getTicker() {
        return ticker;
    }

    /** All price records, oldest first. */
    public List<PriceRecord> getRecords() {
        return new ArrayList<>(records);
    }

    /** The records from start to end (both dates included), oldest first. */
    public List<PriceRecord> getRecordsBetween(LocalDate start, LocalDate end) {
        List<PriceRecord> result = new ArrayList<>();
        for (PriceRecord record : records) {
            LocalDate date = record.getDate();
            if (!date.isBefore(start) && !date.isAfter(end)) {
                result.add(record);
            }
        }
        return result;
    }

    public LocalDate getFirstDate() {
        return records.get(0).getDate();
    }

    public LocalDate getLastDate() {
        return records.get(records.size() - 1).getDate();
    }

    /** The most recent record in the data. */
    public PriceRecord getLatestRecord() {
        return records.get(records.size() - 1);
    }

    public int getNumberOfRecords() {
        return records.size();
    }
}

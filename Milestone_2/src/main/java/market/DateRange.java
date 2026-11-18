package market;

import java.time.LocalDate;

/**
 * A period between two dates, with both dates included, for example
 * 01/01/2025 - 03/31/2025.
 *
 * In Milestone 1 the start and end date were two separate LocalDate values.
 * Milestone 2 needs a period for several stocks at once (Compare Stocks and
 * Sort & Filter), so the two dates are kept together in one object, which
 * also makes sure the start is not after the end.
 */
public class DateRange {

    private final LocalDate start;
    private final LocalDate end;

    /** @throws IllegalArgumentException if the start date is after the end date */
    public DateRange(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("The start date must not be after the end date");
        }
        this.start = start;
        this.end = end;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    /** True if the date is inside the period (the start and end dates count as inside). */
    public boolean contains(LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }
}

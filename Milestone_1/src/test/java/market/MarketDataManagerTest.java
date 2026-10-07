package market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MarketDataManagerTest {

    private static final String HEADER = "Date,Open,High,Low,Close,Volume\n";

    @TempDir
    Path tempFolder; // JUnit creates an empty folder for each test and deletes it afterwards

    @Test
    void loadsTheRealData() throws Exception {
        MarketDataManager manager = new MarketDataManager();
        manager.loadData("data");

        assertEquals(List.of("AAPL", "AMZN", "AVGO", "BRK", "GOOGL", "LLY", "META", "MSFT", "NVDA", "TSLA"),
                manager.getTickers());
        assertTrue(manager.getWarnings().isEmpty());

        Stock nvda = manager.getStock("nvda"); // not case-sensitive
        assertEquals(250, nvda.getNumberOfRecords());
        assertEquals(LocalDate.of(2025, 1, 2), nvda.getFirstDate());
        assertEquals(LocalDate.of(2025, 12, 31), nvda.getLastDate());
    }

    @Test
    void nvdaFirstQuarter2025MatchesValuesCheckedByHand() throws Exception {
        MarketDataManager manager = new MarketDataManager();
        manager.loadData("data");
        List<PriceRecord> q1 = manager.getStock("NVDA")
                .getRecordsBetween(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        MarketAnalyzer analyzer = new MarketAnalyzer();

        assertEquals(60, q1.size());
        assertEquals(149.43, analyzer.findHighestClose(q1).getClose(), 0.001);
        assertEquals(106.98, analyzer.findLowestClose(q1).getClose(), 0.001);
        assertEquals(126.74, analyzer.calculateAverageClose(q1), 0.005);
        assertEquals(-21.64, analyzer.calculatePriceChange(q1), 0.005);
        assertEquals(281_460_689, analyzer.calculateAverageVolume(q1), 1);
        assertEquals(LocalDate.of(2025, 1, 28), analyzer.findBestDay(q1).getDate());
        assertEquals(8.93, analyzer.findBestDay(q1).getPercentChange(), 0.005);
        assertEquals(LocalDate.of(2025, 1, 27), analyzer.findWorstDay(q1).getDate());
        assertEquals(-16.97, analyzer.findWorstDay(q1).getPercentChange(), 0.005);
    }

    @Test
    void unknownTicker() throws Exception {
        MarketDataManager manager = new MarketDataManager();
        manager.loadData("data");
        assertFalse(manager.hasStock("XYZ"));
        assertNull(manager.getStock("XYZ"));
    }

    @Test
    void searchFindsPartsOfTickers() throws Exception {
        MarketDataManager manager = new MarketDataManager();
        manager.loadData("data");
        assertEquals(List.of("AVGO", "GOOGL"), manager.searchTickers("g"));
        assertEquals(10, manager.searchTickers("").size());
    }

    @Test
    void missingFolderThrowsFileNotFoundException() {
        MarketDataManager manager = new MarketDataManager();
        assertThrows(FileNotFoundException.class, () -> manager.loadData("no-such-folder"));
    }

    @Test
    void folderWithoutCsvFilesThrowsFileNotFoundException() throws IOException {
        Files.writeString(tempFolder.resolve("notes.txt"), "hello");
        MarketDataManager manager = new MarketDataManager();
        assertThrows(FileNotFoundException.class, () -> manager.loadData(tempFolder.toString()));
    }

    @Test
    void badRowsAndBadFilesAreSkippedWithWarnings() throws Exception {
        Files.writeString(tempFolder.resolve("GOOD_2025.csv"), HEADER
                + "2025-01-02,10,11,9,10.5,1000\n"
                + "not-a-date,10,11,9,10.5,1000\n"   // bad date
                + "2025-01-03,10,11,9,abc,1000\n"    // bad number
                + "2025-01-06,10,11,9\n"             // missing values
                + "2025-01-07,10,11,9,10.8,1000\n");
        Files.writeString(tempFolder.resolve("WRONG_2025.csv"), "Day,Price\n2025-01-02,10\n");
        Files.writeString(tempFolder.resolve("._GOOD_2025.csv"), "macOS junk"); // hidden file, ignored

        MarketDataManager manager = new MarketDataManager();
        manager.loadData(tempFolder.toString());

        assertEquals(List.of("GOOD"), manager.getTickers());
        assertEquals(2, manager.getStock("GOOD").getNumberOfRecords());
        assertEquals(List.of(
                        "GOOD_2025.csv: skipped 3 invalid row(s)",
                        "WRONG_2025.csv: skipped, the first line must be Date,Open,High,Low,Close,Volume"),
                manager.getWarnings());
    }

    @Test
    void recordsAreSortedByDateEvenIfTheFileIsNot() throws Exception {
        Files.writeString(tempFolder.resolve("ABC.csv"), HEADER
                + "2025-01-03,10,11,9,12,1000\n"
                + "2025-01-02,10,11,9,10,1000\n");
        MarketDataManager manager = new MarketDataManager();
        manager.loadData(tempFolder.toString());

        Stock abc = manager.getStock("ABC");
        assertEquals(LocalDate.of(2025, 1, 2), abc.getFirstDate());
        assertEquals(12, abc.getLatestRecord().getClose(), 0.001);
    }
}

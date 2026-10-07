package market;

import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US); // so numbers always print as 1,234.56

        String dataFolder = "data";
        if (args.length > 0) {
            dataFolder = args[0];
        }

        MarketDataManager dataManager = new MarketDataManager();
        try {
            dataManager.loadData(dataFolder);
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.out.println("Run the program from the project folder, or pass the data folder as an argument.");
            return;
        }

        for (String warning : dataManager.getWarnings()) {
            System.out.println("Warning: " + warning);
        }
        System.out.println("Loaded " + dataManager.getTickers().size() + " stocks: "
                + String.join(", ", dataManager.getTickers()));

        MarketTerminal terminal = new MarketTerminal(dataManager, new Scanner(System.in));
        terminal.run();
    }
}

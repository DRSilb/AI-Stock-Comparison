# Mini Market Terminal (Milestone 1)

A text-based Java program that reads stock prices from CSV files and lets you search for a stock, view
its daily prices, and analyze it over a date range.

## How to run

You need Java 17 or newer. Run these commands from this folder:

**Windows**
```
.\mvnw.cmd package
java -jar target\mini-market-terminal.jar
```

**Mac / Linux**
```
sh mvnw package
java -jar target/mini-market-terminal.jar
```

## How to use it

Type a menu number and press Enter.

```
1. Search Stock
2. View Historical Data
3. Analyze Stock
0. Exit
```

- **Search Stock:** find a stock by its ticker (for example `NVDA`).
- **View Historical Data:** show a stock's daily prices between two dates.
- **Analyze Stock:** show the highest, lowest and average closing price, the
  % price change, the average volume, and the best and worst day.

Type dates as `MM/DD/YYYY`, for example `10/21/2026`.

<img width="725" height="569" alt="UML Diagram" src="https://github.com/user-attachments/assets/b189bf55-a0fe-4178-8194-180a9d7d7ab8" />

## Classes

| Class | What it does |
|---|---|
| `Main` | Starts the program |
| `MarketTerminal` | Shows the menu and reads what the user types |
| `MarketDataManager` | Loads the CSV files |
| `Stock` | One stock and its price history |
| `PriceRecord` | One day of prices |
| `MarketAnalyzer` | Does the calculations |
| `DailyChange` | A day and its % change |

## Data

The `data` folder has 2025 prices for 10 stocks: AAPL, AMZN, AVGO, BRK, GOOGL,
LLY, META, MSFT, NVDA and TSLA.

## Team members

| Member | Tasks |
|---|---|
| Matt F | Worked on loading the data and managing it in the terminal |
| David S | Worked on the math behind the calculations and % changes |

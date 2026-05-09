import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Stocktrading extends JFrame {
    private static final String DATA_FILE = "trading_data.dat";
    private User currentUser;
    private Map<String, User> allUsers = new HashMap<>();
    private Map<String, Stock> marketStocks = new LinkedHashMap<>();

    private JLabel balanceLabel;
    private DefaultTableModel marketTableModel;
    private DefaultTableModel portfolioTableModel;
    private DefaultTableModel transactionTableModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            new Stocktrading().setVisible(true);
        });
    }

    public Stocktrading() {
        initializeMarket();
        loadUserData();

        String name = JOptionPane.showInputDialog(this, "Welcome! Enter your username to login or create an account:", "Login", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            System.exit(0);
        }
        
        String usernameKey = name.trim().toLowerCase();
        if (allUsers.containsKey(usernameKey)) {
            currentUser = allUsers.get(usernameKey);
            JOptionPane.showMessageDialog(this, "Welcome back, " + currentUser.getName() + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
        } else {
            currentUser = new User(name.trim(), 10000.0);
            allUsers.put(usernameKey, currentUser);
            saveUserData();
            JOptionPane.showMessageDialog(this, "New account created! Starting balance: $10,000.00", "Account Created", JOptionPane.INFORMATION_MESSAGE);
        }

        setTitle("Stock Trading Platform - " + currentUser.getName());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveUserData();
                System.exit(0);
            }
        });

        initUI();
        refreshAll();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        balanceLabel = new JLabel("Balance: $0.00");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        balanceLabel.setForeground(new Color(34, 139, 34)); // Forest Green
        topPanel.add(balanceLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh Market Prices");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            simulateMarket();
            refreshAll();
        });
        topPanel.add(refreshBtn, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // 1. Market Tab
        JPanel marketPanel = new JPanel(new BorderLayout(10, 10));
        marketPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        marketTableModel = new DefaultTableModel(new String[]{"Symbol", "Company", "Current Price"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable marketTable = new JTable(marketTableModel);
        marketTable.setRowHeight(25);
        marketTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        marketTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        marketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        marketPanel.add(new JScrollPane(marketTable), BorderLayout.CENTER);
        
        JPanel marketActionPanel = new JPanel();
        JButton buyBtn = new JButton("Buy Selected Stock");
        buyBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        buyBtn.addActionListener(e -> buyStockAction(marketTable));
        marketActionPanel.add(buyBtn);
        marketPanel.add(marketActionPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Live Market", marketPanel);

        // 2. Portfolio Tab
        JPanel portfolioPanel = new JPanel(new BorderLayout(10, 10));
        portfolioPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        portfolioTableModel = new DefaultTableModel(new String[]{"Symbol", "Shares", "Avg Cost", "Current Price", "Total Value", "Profit/Loss"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable portfolioTable = new JTable(portfolioTableModel);
        portfolioTable.setRowHeight(25);
        portfolioTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        portfolioTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        portfolioTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        portfolioPanel.add(new JScrollPane(portfolioTable), BorderLayout.CENTER);

        JPanel portfolioActionPanel = new JPanel();
        JButton sellBtn = new JButton("Sell Selected Stock");
        sellBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sellBtn.addActionListener(e -> sellStockAction(portfolioTable));
        portfolioActionPanel.add(sellBtn);
        portfolioPanel.add(portfolioActionPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("My Portfolio", portfolioPanel);

        // 3. Transactions Tab
        JPanel txPanel = new JPanel(new BorderLayout(10, 10));
        txPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        transactionTableModel = new DefaultTableModel(new String[]{"Date", "Type", "Symbol", "Shares", "Price/Share"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable txTable = new JTable(transactionTableModel);
        txTable.setRowHeight(25);
        txTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        txPanel.add(new JScrollPane(txTable), BorderLayout.CENTER);
        
        tabbedPane.addTab("Transaction History", txPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void refreshAll() {
        balanceLabel.setText(String.format("Available Balance: $%,.2f", currentUser.getBalance()));

        // Refresh Market
        marketTableModel.setRowCount(0);
        for (Stock stock : marketStocks.values()) {
            marketTableModel.addRow(new Object[]{
                stock.getSymbol(), stock.getName(), String.format("$%,.2f", stock.getPrice())
            });
        }

        // Refresh Portfolio
        portfolioTableModel.setRowCount(0);
        for (Map.Entry<String, PortfolioItem> entry : currentUser.getPortfolio().entrySet()) {
            String symbol = entry.getKey();
            PortfolioItem item = entry.getValue();
            Stock stock = marketStocks.get(symbol);
            if (stock != null) {
                double currentPrice = stock.getPrice();
                double value = currentPrice * item.getQuantity();
                double cost = item.getAveragePrice() * item.getQuantity();
                double pl = value - cost;
                portfolioTableModel.addRow(new Object[]{
                    symbol, item.getQuantity(), String.format("$%,.2f", item.getAveragePrice()),
                    String.format("$%,.2f", currentPrice), String.format("$%,.2f", value), 
                    String.format("$%,.2f", pl)
                });
            }
        }

        // Refresh Transactions
        transactionTableModel.setRowCount(0);
        for (Transaction t : currentUser.getTransactions()) {
            transactionTableModel.addRow(new Object[]{
                t.getTimestamp(), t.getType(), t.getSymbol(), t.getQuantity(), String.format("$%,.2f", t.getPrice())
            });
        }
    }

    private void buyStockAction(JTable marketTable) {
        int row = marketTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock from the market list to buy.", "No Stock Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String symbol = (String) marketTableModel.getValueAt(row, 0);
        Stock stock = marketStocks.get(symbol);

        String qtyStr = JOptionPane.showInputDialog(this, 
                "Purchasing: " + stock.getName() + " (" + symbol + ")\n" +
                "Current Price: $" + String.format("%.2f", stock.getPrice()) + "\n" +
                "Available Balance: $" + String.format("%.2f", currentUser.getBalance()) + "\n\n" +
                "Enter quantity to buy:", 
                "Buy Stock", JOptionPane.QUESTION_MESSAGE);
                
        if (qtyStr == null || qtyStr.trim().isEmpty()) return;

        try {
            int quantity = Integer.parseInt(qtyStr.trim());
            if (quantity <= 0) throw new NumberFormatException();
            
            double totalCost = stock.getPrice() * quantity;
            if (currentUser.getBalance() >= totalCost) {
                currentUser.setBalance(currentUser.getBalance() - totalCost);
                currentUser.addPortfolioItem(symbol, quantity, stock.getPrice());
                currentUser.addTransaction(new Transaction("BUY", symbol, quantity, stock.getPrice()));
                JOptionPane.showMessageDialog(this, String.format("Successfully bought %d shares of %s for $%,.2f", quantity, symbol, totalCost), "Purchase Successful", JOptionPane.INFORMATION_MESSAGE);
                saveUserData();
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(this, "Insufficient balance!\nCost: $" + String.format("%,.2f", totalCost) + "\nAvailable: $" + String.format("%,.2f", currentUser.getBalance()), "Insufficient Funds", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive integer for quantity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sellStockAction(JTable portfolioTable) {
        int row = portfolioTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock from your portfolio to sell.", "No Stock Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String symbol = (String) portfolioTableModel.getValueAt(row, 0);
        PortfolioItem item = currentUser.getPortfolio().get(symbol);
        Stock stock = marketStocks.get(symbol);

        String qtyStr = JOptionPane.showInputDialog(this, 
                "Selling: " + stock.getName() + " (" + symbol + ")\n" +
                "Shares Owned: " + item.getQuantity() + "\n" +
                "Current Market Price: $" + String.format("%.2f", stock.getPrice()) + "\n\n" +
                "Enter quantity to sell:", 
                "Sell Stock", JOptionPane.QUESTION_MESSAGE);
                
        if (qtyStr == null || qtyStr.trim().isEmpty()) return;

        try {
            int quantity = Integer.parseInt(qtyStr.trim());
            if (quantity <= 0) throw new NumberFormatException();
            
            if (quantity > item.getQuantity()) {
                JOptionPane.showMessageDialog(this, "You don't have enough shares to sell " + quantity + ".\nYou only own " + item.getQuantity() + " shares.", "Insufficient Shares", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double totalRevenue = stock.getPrice() * quantity;
            currentUser.setBalance(currentUser.getBalance() + totalRevenue);
            currentUser.removePortfolioItem(symbol, quantity);
            currentUser.addTransaction(new Transaction("SELL", symbol, quantity, stock.getPrice()));
            
            JOptionPane.showMessageDialog(this, String.format("Successfully sold %d shares of %s for $%,.2f", quantity, symbol, totalRevenue), "Sale Successful", JOptionPane.INFORMATION_MESSAGE);
            saveUserData();
            refreshAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive integer for quantity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeMarket() {
        marketStocks.put("AAPL", new Stock("AAPL", "Apple Inc.", 150.0));
        marketStocks.put("GOOGL", new Stock("GOOGL", "Alphabet Inc.", 2800.0));
        marketStocks.put("AMZN", new Stock("AMZN", "Amazon.com Inc.", 3400.0));
        marketStocks.put("TSLA", new Stock("TSLA", "Tesla Inc.", 900.0));
        marketStocks.put("MSFT", new Stock("MSFT", "Microsoft Corp.", 300.0));
    }

    private void simulateMarket() {
        Random rand = new Random();
        for (Stock stock : marketStocks.values()) {
            double changePercent = (rand.nextDouble() - 0.5) * 0.05; // -2.5% to +2.5%
            stock.setPrice(Math.max(1.0, stock.getPrice() * (1 + changePercent))); // ensure price doesn't drop below $1
        }
    }

    private void saveUserData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(allUsers);
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadUserData() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object data = ois.readObject();
                if (data instanceof Map) {
                    allUsers = (Map<String, User>) data;
                } else if (data instanceof User) {
                    // Migration from old single-user format
                    User oldUser = (User) data;
                    allUsers.put(oldUser.getName().toLowerCase(), oldUser);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading user data: " + e.getMessage());
            }
        }
    }
}

class Stock {
    private String symbol;
    private String name;
    private double price;

    public Stock(String symbol, String name, double price) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}

class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private double balance;
    private Map<String, PortfolioItem> portfolio;
    private List<Transaction> transactions;

    public User(String name, double balance) {
        this.name = name;
        this.balance = balance;
        this.portfolio = new HashMap<>();
        this.transactions = new ArrayList<>();
    }

    public String getName() { return name; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public Map<String, PortfolioItem> getPortfolio() { return portfolio; }
    public List<Transaction> getTransactions() { return transactions; }

    public void addPortfolioItem(String symbol, int quantity, double price) {
        if (portfolio.containsKey(symbol)) {
            PortfolioItem item = portfolio.get(symbol);
            double totalCost = (item.getQuantity() * item.getAveragePrice()) + (quantity * price);
            int newQuantity = item.getQuantity() + quantity;
            item.setAveragePrice(totalCost / newQuantity);
            item.setQuantity(newQuantity);
        } else {
            portfolio.put(symbol, new PortfolioItem(quantity, price));
        }
    }

    public void removePortfolioItem(String symbol, int quantity) {
        if (portfolio.containsKey(symbol)) {
            PortfolioItem item = portfolio.get(symbol);
            int newQuantity = item.getQuantity() - quantity;
            if (newQuantity <= 0) {
                portfolio.remove(symbol);
            } else {
                item.setQuantity(newQuantity);
            }
        }
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }
}

class PortfolioItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private int quantity;
    private double averagePrice;

    public PortfolioItem(int quantity, double averagePrice) {
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getAveragePrice() { return averagePrice; }
    public void setAveragePrice(double averagePrice) { this.averagePrice = averagePrice; }
}

class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;
    private String symbol;
    private int quantity;
    private double price;
    private String timestamp;

    public Transaction(String type, String symbol, int quantity, double price) {
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = new java.util.Date().toString();
    }

    public String getType() { return type; }
    public String getSymbol() { return symbol; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getTimestamp() { return timestamp; }
}

package com.stocksim;

import io.github.cdimascio.dotenv.Dotenv;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainFrame extends JFrame {
    private final Portfolio portfolio = new Portfolio(10_000.0);
    private final Map<String, Double> priceCache = new HashMap<>();
    private StockService stockService;

    // UI components
    private JTabbedPane tabs;
    private ChartPanelView chartPanel;
    private PortfolioPanel portfolioPanel;
    private JTextField tickerField;
    private JLabel priceLabel;
    private JLabel statusLabel;
    private JSpinner sharesSpinner;
    private String currentTicker = null;
    private double currentPrice = 0;

    public MainFrame() {
        loadApiKey();
        buildUI();
        setTitle("Stock Market Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 550));
        setLocationRelativeTo(null);
    }

    private void loadApiKey() {
        String key = null;
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            key = dotenv.get("ALPHA_VANTAGE_KEY");
        } catch (Exception ignored) {}

        if (key == null || key.isBlank()) {
            key = JOptionPane.showInputDialog(null,
                "Enter your Alpha Vantage API key:\n(Get one free at alphavantage.co)",
                "API Key Required", JOptionPane.PLAIN_MESSAGE);
        }
        if (key == null || key.isBlank()) {
            JOptionPane.showMessageDialog(null, "No API key provided. Exiting.");
            System.exit(0);
        }
        stockService = new StockService(key.trim());
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(18, 18, 30));

        // Top bar
        root.add(buildTopBar(), BorderLayout.NORTH);

        // Tabs
        tabs = new JTabbedPane();
        tabs.setBackground(new Color(18, 18, 30));
        tabs.setForeground(Color.WHITE);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Trade tab
        JSplitPane tradePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTradePanel(), buildChartArea());
        tradePane.setDividerLocation(300);
        tradePane.setBackground(new Color(18, 18, 30));
        tabs.addTab("📈 Trade", tradePane);

        // Portfolio tab
        portfolioPanel = new PortfolioPanel(portfolio, priceCache);
        tabs.addTab("💼 Portfolio", portfolioPanel);

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) portfolioPanel.refresh();
        });

        root.add(tabs, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel("Ready. Search for a stock to begin.");
        statusLabel.setForeground(new Color(120, 120, 160));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setBorder(new EmptyBorder(4, 12, 4, 12));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(12, 12, 22));
        root.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildTopBar() {
        // top
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(new Color(28, 28, 45));

        JLabel logo = new JLabel("💹 StockSim");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(new Color(100, 200, 255));
        bar.add(logo);
        return bar;
    }

    private JPanel buildTradePanel() {
        // left side
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(22, 22, 36));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Search
        addSection(panel, "SEARCH STOCK");
        tickerField = darkField();
        tickerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tickerField.addActionListener(e -> searchStock());
        panel.add(tickerField);
        panel.add(Box.createVerticalStrut(8));

        JButton searchBtn = accentButton("Search");
        searchBtn.addActionListener(e -> searchStock());
        panel.add(searchBtn);
        panel.add(Box.createVerticalStrut(16));

        // Price display
        addSection(panel, "CURRENT PRICE");
        priceLabel = new JLabel("—");
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        priceLabel.setForeground(new Color(100, 220, 100));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(priceLabel);
        panel.add(Box.createVerticalStrut(16));

        // Shares
        addSection(panel, "SHARES");
        sharesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        sharesSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        styleSpinner(sharesSpinner);
        panel.add(sharesSpinner);
        panel.add(Box.createVerticalStrut(12));

        // Buy/Sell buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setBackground(new Color(22, 22, 36));
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton buyBtn = colorButton("BUY", new Color(50, 160, 80));
        JButton sellBtn = colorButton("SELL", new Color(180, 50, 50));
        buyBtn.addActionListener(e -> tradeAction(true));
        sellBtn.addActionListener(e -> tradeAction(false));
        btnRow.add(buyBtn);
        btnRow.add(sellBtn);
        panel.add(btnRow);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel buildChartArea() {
        chartPanel = new ChartPanelView();
        return chartPanel;
    }

    private void searchStock() {
        String ticker = tickerField.getText().trim().toUpperCase();
        if (ticker.isEmpty()) return;
        setStatus("Fetching price for " + ticker + "...");
        priceLabel.setText("...");

        new SwingWorker<double[], Void>() {
            double[] result;
            Exception error;

            protected double[] doInBackground() throws Exception {
                double price = stockService.getCurrentPrice(ticker);
                return new double[]{price};
            }

            protected void done() {
                try {
                    double price = get()[0];
                    currentTicker = ticker;
                    currentPrice = price;
                    priceCache.put(ticker, price);
                    priceLabel.setText(String.format("$%.2f", price));
                    setStatus(ticker + " loaded. Loading chart...");
                    loadChart(ticker);
                } catch (Exception ex) {
                    setStatus("Error: " + ex.getMessage());
                    priceLabel.setText("—");
                }
            }
        }.execute();
    }

    private void loadChart(String ticker) {
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                var history = stockService.getDailyHistory(ticker);
                SwingUtilities.invokeLater(() -> chartPanel.showChart(ticker, history));
                return null;
            }
            protected void done() {
                try { get(); setStatus(ticker + " ready."); }
                catch (Exception e) { setStatus("Chart error: " + e.getMessage()); }
            }
        }.execute();
    }

    private void tradeAction(boolean isBuy) {
        if (currentTicker == null || currentPrice == 0) {
            setStatus("Search for a stock first.");
            return;
        }
        int shares = (int) sharesSpinner.getValue();
        boolean ok = isBuy
            ? portfolio.buy(currentTicker, shares, currentPrice)
            : portfolio.sell(currentTicker, shares, currentPrice);

        if (ok) {
            setStatus(String.format("%s %d shares of %s @ $%.2f",
                isBuy ? "Bought" : "Sold", shares, currentTicker, currentPrice));
        } else {
            setStatus(isBuy ? "Not enough cash!" : "Not enough shares!");
        }
    }

    private void addSection(JPanel panel, String title) {
        JLabel lbl = new JLabel(title);
        lbl.setForeground(new Color(100, 100, 140));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));
    }

    private JTextField darkField() {
        JTextField f = new JTextField();
        f.setBackground(new Color(35, 35, 55));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 90)),
            new EmptyBorder(6, 8, 6, 8)));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private JButton accentButton(String text) {
        return colorButton(text, new Color(60, 100, 200));
    }

    private JButton colorButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return btn;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBackground(new Color(35, 35, 55));
        spinner.setForeground(Color.WHITE);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(new Color(35, 35, 55));
            de.getTextField().setForeground(Color.WHITE);
            de.getTextField().setCaretColor(Color.WHITE);
            de.getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        spinner.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(msg));
    }
}

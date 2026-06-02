package com.stocksim;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Map;

public class PortfolioPanel extends JPanel {
    private final Portfolio portfolio;
    private final Map<String, Double> priceCache;
    private final DefaultTableModel tableModel;
    private final JLabel cashLabel;
    private final JLabel totalLabel;
    private final JLabel plLabel;

    private static final double STARTING_CASH = 10_000.0;

    public PortfolioPanel(Portfolio portfolio, Map<String, Double> priceCache) {
        this.portfolio = portfolio;
        this.priceCache = priceCache;
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(18, 18, 30));

        // Header
        JPanel header = new JPanel(new GridLayout(1, 3, 20, 0));
        header.setBackground(new Color(28, 28, 45));
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        cashLabel = statLabel("Cash", "$10,000.00");
        totalLabel = statLabel("Total Value", "$10,000.00");
        plLabel = statLabel("P&L", "$0.00");

        header.add(cashLabel);
        header.add(totalLabel);
        header.add(plLabel);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Ticker", "Shares", "Avg Buy", "Current", "Value", "P&L"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setBackground(new Color(22, 22, 36));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(50, 50, 70));
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(35, 35, 55));
        table.getTableHeader().setForeground(Color.LIGHT_GRAY);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionBackground(new Color(60, 60, 100));

        // Color P&L column
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                setForeground(val != null && val.toString().startsWith("-") ? new Color(255, 100, 100) : new Color(100, 220, 100));
                setBackground(sel ? new Color(60, 60, 100) : new Color(22, 22, 36));
                setHorizontalAlignment(RIGHT);
                return this;
            }
        });
        // Right-align numeric columns
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(JLabel.RIGHT);
        for (int i = 1; i <= 4; i++) table.getColumnModel().getColumn(i).setCellRenderer(right);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JLabel statLabel(String title, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(28, 28, 45));
        JLabel t = new JLabel(title);
        t.setForeground(new Color(120, 120, 160));
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel v = new JLabel(value);
        v.setForeground(Color.WHITE);
        v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        // Store value label in a wrapper we can retrieve
        JLabel wrapper = new JLabel(title + "|" + value);
        wrapper.putClientProperty("title", t);
        wrapper.putClientProperty("value", v);
        // Return the value label so we can update it
        // Actually return a combined panel embedded label
        add(p); // temp add to get layout — we'll restructure
        return v;
    }

    public void refresh() {
        double cash = portfolio.getCash();
        double total = portfolio.getTotalValue(priceCache);
        double pl = total - STARTING_CASH;

        cashLabel.setText(String.format("$%,.2f", cash));
        totalLabel.setText(String.format("$%,.2f", total));
        plLabel.setText(String.format("%s$%,.2f", pl >= 0 ? "+" : "", pl));
        plLabel.setForeground(pl >= 0 ? new Color(100, 220, 100) : new Color(255, 100, 100));

        tableModel.setRowCount(0);
        for (Map.Entry<String, Integer> e : portfolio.getHoldings().entrySet()) {
            String ticker = e.getKey();
            int shares = e.getValue();
            double avg = portfolio.getAvgBuyPrice(ticker);
            double current = priceCache.getOrDefault(ticker, 0.0);
            double value = shares * current;
            double rowPL = (current - avg) * shares;
            tableModel.addRow(new Object[]{
                ticker, shares,
                String.format("$%.2f", avg),
                String.format("$%.2f", current),
                String.format("$%,.2f", value),
                String.format("%s$%,.2f", rowPL >= 0 ? "+" : "", rowPL)
            });
        }
    }
}

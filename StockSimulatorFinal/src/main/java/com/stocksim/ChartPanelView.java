package com.stocksim;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;

public class ChartPanelView extends JPanel {

    private final JPanel placeholder;
    private ChartPanel chartPanel;

    public ChartPanelView() {

        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 30));

        placeholder = new JPanel(new GridBagLayout());
        placeholder.setBackground(new Color(18, 18, 30));

        JLabel lbl = new JLabel("Search a stock to see its chart");
        lbl.setForeground(new Color(120, 120, 160));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        placeholder.add(lbl);

        add(placeholder, BorderLayout.CENTER);
    }

    public void showChart(String ticker, Map<LocalDate, Double> history) {

        TimeSeries series = new TimeSeries(ticker);

        for (Map.Entry<LocalDate, Double> entry : history.entrySet()) {

            LocalDate d = entry.getKey();

            series.add(
                new Day(
                    d.getDayOfMonth(),
                    d.getMonthValue(),
                    d.getYear()
                ),
                entry.getValue()
            );
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                ticker + " Stock Price",
                "Date",
                "Price",
                dataset,
                false,
                true,
                false
        );

        chart.setBackgroundPaint(new Color(18, 18, 30));

        XYPlot plot = chart.getXYPlot();

        plot.setBackgroundPaint(new Color(28, 28, 45));

        plot.setDomainGridlinePaint(new Color(70, 70, 90));
        plot.setRangeGridlinePaint(new Color(70, 70, 90));

        plot.getDomainAxis().setLabelPaint(Color.WHITE);
        plot.getRangeAxis().setLabelPaint(Color.WHITE);

        plot.getDomainAxis().setTickLabelPaint(Color.LIGHT_GRAY);
        plot.getRangeAxis().setTickLabelPaint(Color.LIGHT_GRAY);

        chart.getTitle().setPaint(Color.WHITE);

        XYLineAndShapeRenderer renderer =
                new XYLineAndShapeRenderer(true, false);

        renderer.setSeriesPaint(0, new Color(100, 200, 255));
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));

        plot.setRenderer(renderer);

        if (chartPanel != null) {
            remove(chartPanel);
        }

        remove(placeholder);

        chartPanel = new ChartPanel(chart);

        chartPanel.setMouseWheelEnabled(true);

        chartPanel.setBackground(new Color(18, 18, 30));

        add(chartPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }
}
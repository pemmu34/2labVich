package org.example;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Main extends JFrame {

    // Исходные данные (Таблица 1 из методички)
    static final double[] X = {-0.1, -0.2, -0.3, -0.4, -0.5, -0.6, -0.7};
    static final double[] Y = {21.0, 12.0, 8.0, 7.0, 10.0, 16.0, 13.0};

    JLabel coeffLabel;
    JTable table;
    ChartPanel chartPanel;

    public Main() {
        setTitle("Лабораторная работа №2: Метод наименьших квадратов");
        setSize(1300, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Настройка шрифтов
        UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 16));
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 16));
        UIManager.put("Table.font", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("Table.rowHeight", 25);

        // ВЕРХНЯЯ ПАНЕЛЬ
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Формулы
        String formulaHtml = "<html><body style='font-family: Arial; font-size: 15px;'>" +
                "<h2 style='margin-top: 0;'>Метод наименьших квадратов (МНК)</h2>" +
                "<p><b>Ищем зависимость:</b> G(x) = a<sub>0</sub> + a<sub>1</sub>x + a<sub>2</sub>x<sup>2</sup></p>" +
                "<p><b>Минимизируем сумму квадратов ошибок:</b> E = &Sigma; (G(x<sub>i</sub>) - y<sub>i</sub>)<sup>2</sup> &rarr; min</p>" +
                "<p><b>Система нормальных уравнений:</b><br>" +
                "(n) &middot; a<sub>0</sub> + (&Sigma;x<sub>i</sub>) &middot; a<sub>1</sub> + (&Sigma;x<sub>i</sub><sup>2</sup>) &middot; a<sub>2</sub> = &Sigma;y<sub>i</sub><br>" +
                "(&Sigma;x<sub>i</sub>) &middot; a<sub>0</sub> + (&Sigma;x<sub>i</sub><sup>2</sup>) &middot; a<sub>1</sub> + (&Sigma;x<sub>i</sub><sup>3</sup>) &middot; a<sub>2</sub> = &Sigma;x<sub>i</sub>y<sub>i</sub><br>" +
                "(&Sigma;x<sub>i</sub><sup>2</sup>) &middot; a<sub>0</sub> + (&Sigma;x<sub>i</sub><sup>3</sup>) &middot; a<sub>1</sub> + (&Sigma;x<sub>i</sub><sup>4</sup>) &middot; a<sub>2</sub> = &Sigma;x<sub>i</sub><sup>2</sup>y<sub>i</sub></p>" +
                "</body></html>";
        topPanel.add(new JLabel(formulaHtml), BorderLayout.CENTER);

        // Функционал кнопки "Аппроксимировать"
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton calcBtn = new JButton("Аппроксимировать");
        calcBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        calcBtn.setPreferredSize(new Dimension(200, 40));
        calcBtn.setMaximumSize(new Dimension(200, 40));

        coeffLabel = new JLabel("Коэффициенты: не рассчитаны");
        coeffLabel.setFont(new Font("Arial", Font.BOLD, 16));
        coeffLabel.setForeground(new Color(0, 100, 0));
        coeffLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        controlPanel.add(calcBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        controlPanel.add(coeffLabel);

        topPanel.add(controlPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ЦЕНТРАЛЬНАЯ ПАНЕЛЬ
        table = new JTable();
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Таблица сумм", 0, 0, new Font("Arial", Font.BOLD, 14), Color.DARK_GRAY));

        chartPanel = new ChartPanel(null);
        chartPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "График", 0, 0, new Font("Arial", Font.BOLD, 14), Color.DARK_GRAY));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, chartPanel);
        splitPane.setResizeWeight(0.4); // 40% ширины для таблицы, 60% для графика
        splitPane.setDividerLocation(500);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(splitPane, BorderLayout.CENTER);

        // Слушатели и инициализация
        calcBtn.addActionListener(e -> calculateAndDraw());

        // Инициализируем пустую таблицу и график только с исходными точками
        initTableAndChart();
    }

    /**
     * Первоначальная отрисовка (без расчетов)
     */
    private void initTableAndChart() {
        int n = X.length;
        Object[][] tableData = new Object[n][3];
        String[] colNames = {"i", "x_i", "y_i"};

        for (int i = 0; i < n; i++) {
            tableData[i][0] = i;
            tableData[i][1] = String.format("%.2f", X[i]);
            tableData[i][2] = String.format("%.2f", Y[i]);
        }

        table.setModel(new DefaultTableModel(tableData, colNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);

        // График только с точками
        drawChart(null, null, null);
    }

    /**
     * Основной расчет МНК и отрисовка
     */
    private void calculateAndDraw() {
        int n = X.length;

        // 1. Вычисление сумм
        double sumX = 0, sumX2 = 0, sumX3 = 0, sumX4 = 0;
        double sumY = 0, sumXY = 0, sumX2Y = 0;

        Object[][] tableData = new Object[n + 1][8]; // +1 для строки сумм
        String[] colNames = {"i", "x_i", "y_i", "x_i^2", "x_i^3", "x_i^4", "x_i*y_i", "x_i^2*y_i"};

        for (int i = 0; i < n; i++) {
            double x = X[i];
            double y = Y[i];
            double x2 = x * x;
            double x3 = x2 * x;
            double x4 = x3 * x;

            sumX += x; sumX2 += x2; sumX3 += x3; sumX4 += x4;
            sumY += y; sumXY += x * y; sumX2Y += x2 * y;

            tableData[i][0] = i;
            tableData[i][1] = String.format("%.2f", x);
            tableData[i][2] = String.format("%.2f", y);
            tableData[i][3] = String.format("%.4f", x2);
            tableData[i][4] = String.format("%.4f", x3);
            tableData[i][5] = String.format("%.4f", x4);
            tableData[i][6] = String.format("%.4f", x * y);
            tableData[i][7] = String.format("%.4f", x2 * y);
        }

        // Строка сумм
        tableData[n][0] = "Σ";
        tableData[n][1] = String.format("%.2f", sumX);
        tableData[n][2] = String.format("%.2f", sumY);
        tableData[n][3] = String.format("%.4f", sumX2);
        tableData[n][4] = String.format("%.4f", sumX3);
        tableData[n][5] = String.format("%.4f", sumX4);
        tableData[n][6] = String.format("%.4f", sumXY);
        tableData[n][7] = String.format("%.4f", sumX2Y);

        table.setModel(new DefaultTableModel(tableData, colNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(30);

        // 2. Матрица системы A и вектор B
        double[][] A = {
                {n, sumX, sumX2},
                {sumX, sumX2, sumX3},
                {sumX2, sumX3, sumX4}
        };
        double[] B = {sumY, sumXY, sumX2Y};

        // 3. Метод Крамера
        double delta = det3(A);
        if (Math.abs(delta) < 1e-9) {
            coeffLabel.setText("Ошибка: Система вырождена");
            coeffLabel.setForeground(Color.RED);
            return;
        }

        double a0 = det3(replaceColumn(A, B, 0)) / delta;
        double a1 = det3(replaceColumn(A, B, 1)) / delta;
        double a2 = det3(replaceColumn(A, B, 2)) / delta;

        coeffLabel.setText(String.format("<html>y = %.4f + (%.4f)·x + (%.4f)·x<sup>2</sup></html>", a0, a1, a2));
        coeffLabel.setForeground(new Color(0, 100, 0));

        // Отрисовка графика
        drawChart(a0, a1, a2);
    }

    /**
     * Отрисовка графика. Если коэффициенты null — рисуем только точки.
     */
    private void drawChart(Double a0, Double a1, Double a2) {
        XYSeries pointsSeries = new XYSeries("Исходные точки");
        for (int i = 0; i < X.length; i++) {
            pointsSeries.add(X[i], Y[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();

        // Если коэффициенты есть, строим кривую
        if (a0 != null && a1 != null && a2 != null) {
            XYSeries curveSeries = new XYSeries("Аппроксимирующая парабола");
            double minX = X[0], maxX = X[0];
            for (double x : X) {
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
            }
            double step = (maxX - minX) / 100.0;
            for (double cx = minX - 0.05; cx <= maxX + 0.05; cx += step) {
                curveSeries.add(cx, a0 + a1 * cx + a2 * cx * cx);
            }
            dataset.addSeries(curveSeries);
        }

        dataset.addSeries(pointsSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "МНК: Квадратичная аппроксимация", "x", "y", dataset, PlotOrientation.VERTICAL, true, true, false);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();

        if (a0 != null) {
            // Кривая
            renderer.setSeriesLinesVisible(0, true);
            renderer.setSeriesShapesVisible(0, false);
            renderer.setSeriesPaint(0, Color.BLUE);
            // Точки
            renderer.setSeriesLinesVisible(1, false);
            renderer.setSeriesShapesVisible(1, true);
            renderer.setSeriesPaint(1, Color.RED);
        } else {
            // Только точки
            renderer.setSeriesLinesVisible(0, false);
            renderer.setSeriesShapesVisible(0, true);
            renderer.setSeriesPaint(0, Color.RED);
        }

        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultEntityRadius(6);
        chart.getXYPlot().setBackgroundPaint(Color.WHITE);
        chart.getXYPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);
        chart.getXYPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);

        chartPanel.setChart(chart);
    }

    // Вспомогательные методы
    private double det3(double[][] m) {
        return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
                - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
                + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
    }

    private double[][] replaceColumn(double[][] m, double[] col, int index) {
        double[][] res = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                res[i][j] = (j == index) ? col[i] : m[i][j];
            }
        }
        return res;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new Main().setVisible(true);
        });
    }
}
package dao;

import db.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class ReportDAO {

    public Map<String, Object> getMonthlyReport(String yearMonth) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("month", yearMonth);

        addMonthlySummary(report, yearMonth);

        report.put("categoryBreakdown", getCategoryBreakdown(yearMonth));

        report.put("topCategory", getTopCategory(yearMonth));

        report.put("dailyPattern", getDailyPattern(yearMonth));

        return report;
    }

    public List<String> getAvailableMonths() {
        List<String> months = new ArrayList<>();
        String sql = "SELECT DISTINCT DATE_FORMAT(date, '%Y-%m') AS month " +
                     "FROM transactions ORDER BY month DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                months.add(rs.getString("month"));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching available months: " + e.getMessage());
        }
        return months;
    }

    private void addMonthlySummary(Map<String, Object> report, String yearMonth) {
        String sql =
            "SELECT " +
            "  SUM(CASE WHEN type='Income'  THEN amount ELSE 0 END) AS income, " +
            "  SUM(CASE WHEN type='Expense' THEN amount ELSE 0 END) AS expense " +
            "FROM transactions " +
            "WHERE DATE_FORMAT(date, '%Y-%m') = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, yearMonth);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal income  = nullSafe(rs.getBigDecimal("income"));
                    BigDecimal expense = nullSafe(rs.getBigDecimal("expense"));
                    BigDecimal savings = income.subtract(expense);

                    double savingsRate = 0.0;
                    if (income.compareTo(BigDecimal.ZERO) > 0) {
                        savingsRate = savings.doubleValue() / income.doubleValue() * 100.0;
                    }

                    report.put("income",      income);
                    report.put("expense",     expense);
                    report.put("savings",     savings);
                    report.put("savingsRate", Math.round(savingsRate * 10.0) / 10.0);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching monthly summary: " + e.getMessage());
        }
    }

    private Map<String, BigDecimal> getCategoryBreakdown(String yearMonth) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        String sql =
            "SELECT category, SUM(amount) AS total " +
            "FROM transactions " +
            "WHERE type='Expense' AND DATE_FORMAT(date, '%Y-%m') = ? " +
            "GROUP BY category ORDER BY total DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, yearMonth);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("category"), rs.getBigDecimal("total"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching category breakdown: " + e.getMessage());
        }
        return result;
    }

    private String getTopCategory(String yearMonth) {
        String sql =
            "SELECT category FROM transactions " +
            "WHERE type='Expense' AND DATE_FORMAT(date, '%Y-%m') = ? " +
            "GROUP BY category ORDER BY SUM(amount) DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, yearMonth);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("category");
            }

        } catch (SQLException e) {
            System.err.println("Error fetching top category: " + e.getMessage());
        }
        return "N/A";
    }

    private Map<String, BigDecimal> getDailyPattern(String yearMonth) {
        Map<String, BigDecimal> pattern = new LinkedHashMap<>();

        String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
        for (String d : days) pattern.put(d, BigDecimal.ZERO);

        String sql =
            "SELECT DAYNAME(date) AS dayName, SUM(amount) AS total " +
            "FROM transactions " +
            "WHERE type='Expense' AND DATE_FORMAT(date, '%Y-%m') = ? " +
            "GROUP BY DAYNAME(date), DAYOFWEEK(date) " +
            "ORDER BY DAYOFWEEK(date)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, yearMonth);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pattern.put(rs.getString("dayName"), rs.getBigDecimal("total"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching daily pattern: " + e.getMessage());
        }
        return pattern;
    }

    private BigDecimal nullSafe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}

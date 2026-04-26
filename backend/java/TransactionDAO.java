package dao;

import db.DBConnection;
import model.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionDAO {

    public boolean addTransaction(Transaction t) {
        String sql = "INSERT INTO transactions (date, type, category, amount, description) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(t.getDate()));   
            ps.setString(2, t.getType());
            ps.setString(3, t.getCategory());
            ps.setBigDecimal(4, t.getAmount());
            ps.setString(5, t.getDescription());

            int rowsAffected = ps.executeUpdate();      
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error adding transaction: " + e.getMessage());
            return false;
        }
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {         

            while (rs.next()) {                          
                list.add(mapRow(rs));                    
            }

        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return list;
    }

    public List<Transaction> getFilteredTransactions(String type, String category) {
        List<Transaction> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE 1=1");
        if (type     != null && !type.isEmpty())     sql.append(" AND type = ?");
        if (category != null && !category.isEmpty()) sql.append(" AND category = ?");
        sql.append(" ORDER BY date DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (type     != null && !type.isEmpty())     ps.setString(paramIndex++, type);
            if (category != null && !category.isEmpty()) ps.setString(paramIndex,   category);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error filtering transactions: " + e.getMessage());
        }
        return list;
    }

    public Transaction getTransactionById(int id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching transaction by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean updateTransaction(Transaction t) {
        String sql = "UPDATE transactions " +
                     "SET date=?, type=?, category=?, amount=?, description=? " +
                     "WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(t.getDate()));
            ps.setString(2, t.getType());
            ps.setString(3, t.getCategory());
            ps.setBigDecimal(4, t.getAmount());
            ps.setString(5, t.getDescription());
            ps.setInt(6, t.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            return false;
        }
    }

    public Map<String, BigDecimal> getDashboardSummary() {
        Map<String, BigDecimal> summary = new HashMap<>();
        String sql = "SELECT " +
                     "  SUM(CASE WHEN type='Income'  THEN amount ELSE 0 END) AS totalIncome, " +
                     "  SUM(CASE WHEN type='Expense' THEN amount ELSE 0 END) AS totalExpense " +
                     "FROM transactions";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                BigDecimal income  = rs.getBigDecimal("totalIncome");
                BigDecimal expense = rs.getBigDecimal("totalExpense");
                if (income  == null) income  = BigDecimal.ZERO;
                if (expense == null) expense = BigDecimal.ZERO;

                summary.put("totalIncome",   income);
                summary.put("totalExpense",  expense);
                summary.put("cashFlow",      income.subtract(expense));
                summary.put("savings",       income.subtract(expense));  
            }

        } catch (SQLException e) {
            System.err.println("Error fetching dashboard summary: " + e.getMessage());
        }
        return summary;
    }

    public Map<String, BigDecimal> getExpenseByCategory() {
        Map<String, BigDecimal> result = new HashMap<>();
        String sql = "SELECT category, SUM(amount) AS total " +
                     "FROM transactions " +
                     "WHERE type = 'Expense' " +
                     "GROUP BY category " +
                     "ORDER BY total DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.put(rs.getString("category"), rs.getBigDecimal("total"));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching expense by category: " + e.getMessage());
        }
        return result;
    }

    public List<Map<String, Object>> getMonthlyTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT " +
                     "  DATE_FORMAT(date, '%Y-%m') AS month, " +
                     "  SUM(CASE WHEN type='Income'  THEN amount ELSE 0 END) AS income, " +
                     "  SUM(CASE WHEN type='Expense' THEN amount ELSE 0 END) AS expense " +
                     "FROM transactions " +
                     "WHERE date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                     "GROUP BY month " +
                     "ORDER BY month ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("month",   rs.getString("month"));
                row.put("income",  rs.getBigDecimal("income"));
                row.put("expense", rs.getBigDecimal("expense"));
                result.add(row);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching monthly trend: " + e.getMessage());
        }
        return result;
    }

    public String getHighestSpendingCategory() {
        String sql = "SELECT category " +
                     "FROM transactions " +
                     "WHERE type='Expense' " +
                     "  AND MONTH(date)=MONTH(CURDATE()) " +
                     "  AND YEAR(date)=YEAR(CURDATE()) " +
                     "GROUP BY category " +
                     "ORDER BY SUM(amount) DESC " +
                     "LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getString("category");
            }

        } catch (SQLException e) {
            System.err.println("Error fetching highest spending category: " + e.getMessage());
        }
        return "N/A";
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setDate(rs.getDate("date").toLocalDate());   
        t.setType(rs.getString("type"));
        t.setCategory(rs.getString("category"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setDescription(rs.getString("description"));
        return t;
    }
}

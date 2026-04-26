package dao;

import db.DBConnection;
import model.Budget;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {

    public List<Budget> getAllBudgets() {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT * FROM budgets ORDER BY category ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching budgets: " + e.getMessage());
        }
        return list;
    }

    public BigDecimal getLimitForCategory(String category) {
        String sql = "SELECT monthly_limit FROM budgets WHERE category = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("monthly_limit");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching budget limit: " + e.getMessage());
        }
        return null;
    }

    public int addBudget(Budget b) {
        String sql = "INSERT INTO budgets (category, monthly_limit) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, b.getCategory());
            ps.setBigDecimal(2, b.getMonthlyLimit());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error adding budget: " + e.getMessage());
        }
        return -1;
    }

    public boolean updateBudget(Budget b) {
        String sql = "UPDATE budgets SET category = ?, monthly_limit = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, b.getCategory());
            ps.setBigDecimal(2, b.getMonthlyLimit());
            ps.setInt(3, b.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating budget: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteBudget(int id) {
        String sql = "DELETE FROM budgets WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting budget: " + e.getMessage());
            return false;
        }
    }

    public List<Budget> getBudgetVsSpending() {
        List<Budget> list = new ArrayList<>();

        String sql =
            "SELECT b.id, b.category, b.monthly_limit, " +
            "       COALESCE(t.spent, 0) AS spent, " +
            "       (b.monthly_limit - COALESCE(t.spent, 0)) AS remaining " +
            "FROM budgets b " +
            "LEFT JOIN ( " +
            "    SELECT category, SUM(amount) AS spent " +
            "    FROM transactions " +
            "    WHERE type = 'Expense' " +
            "      AND MONTH(date) = MONTH(CURDATE()) " +
            "      AND YEAR(date)  = YEAR(CURDATE()) " +
            "    GROUP BY category " +
            ") t ON b.category = t.category " +
            "ORDER BY b.category ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Budget bud = new Budget();
                bud.setId(rs.getInt("id"));
                bud.setCategory(rs.getString("category"));
                bud.setMonthlyLimit(rs.getBigDecimal("monthly_limit"));
                bud.setSpent(rs.getBigDecimal("spent"));
                bud.setRemaining(rs.getBigDecimal("remaining"));
                list.add(bud);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching budget vs spending: " + e.getMessage());
        }
        return list;
    }

    private Budget mapRow(ResultSet rs) throws SQLException {
        Budget b = new Budget();
        b.setId(rs.getInt("id"));
        b.setCategory(rs.getString("category"));
        b.setMonthlyLimit(rs.getBigDecimal("monthly_limit"));
        return b;
    }
}

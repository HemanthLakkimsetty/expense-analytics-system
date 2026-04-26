package dao;

import db.DBConnection;
import model.Budget;
import model.Category;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY type DESC, name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("type")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
        }
        return list;
    }

    public List<Category> getCategoriesByType(String type) {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE type = ? ORDER BY name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching categories by type: " + e.getMessage());
        }
        return list;
    }
}

class BudgetDAO {

    public List<Budget> getAllBudgets() {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT * FROM budgets ORDER BY category ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Budget(
                    rs.getInt("id"),
                    rs.getString("category"),
                    rs.getBigDecimal("monthly_limit")
                ));
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

    public boolean addBudget(Budget b) {
        String sql = "INSERT INTO budgets (category, monthly_limit) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, b.getCategory());
            ps.setBigDecimal(2, b.getMonthlyLimit());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding budget: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBudget(Budget b) {
        String sql = "UPDATE budgets SET monthly_limit = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, b.getMonthlyLimit());
            ps.setInt(2, b.getId());
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
}

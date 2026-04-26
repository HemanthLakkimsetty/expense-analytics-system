package servlet;

import com.google.gson.Gson;
import dao.TransactionDAO;
import dao.CategoryDAO;
import model.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/api/*")
public class FinanceServlet extends HttpServlet {

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final CategoryDAO    categoryDAO    = new CategoryDAO();
    private final Gson           gson           = new Gson();   

    private void setupResponse(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");   
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setupResponse(resp);
        String path = req.getPathInfo();    

        try (PrintWriter out = resp.getWriter()) {

            if ("/transactions".equals(path)) {

                String type     = req.getParameter("type");
                String category = req.getParameter("category");

                List<Transaction> list = transactionDAO.getFilteredTransactions(type, category);
                out.print(gson.toJson(serializeTransactions(list)));

            } else if ("/dashboard".equals(path)) {
                Map<String, BigDecimal> summary = transactionDAO.getDashboardSummary();
                out.print(gson.toJson(summary));

            } else if ("/analytics/category".equals(path)) {
                Map<String, BigDecimal> data = transactionDAO.getExpenseByCategory();
                out.print(gson.toJson(data));

            } else if ("/analytics/monthly".equals(path)) {
                List<Map<String, Object>> data = transactionDAO.getMonthlyTrend();
                out.print(gson.toJson(data));

            } else if ("/analytics/top-category".equals(path)) {
                String top = transactionDAO.getHighestSpendingCategory();
                out.print(gson.toJson(Map.of("category", top)));

            } else if ("/categories".equals(path)) {
                out.print(gson.toJson(categoryDAO.getAllCategories()));

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Unknown endpoint: " + path)));
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setupResponse(resp);
        req.setCharacterEncoding("UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            Transaction t = parseTransactionFromRequest(req);

            boolean success = transactionDAO.addTransaction(t);
            if (success) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(Map.of("message", "Transaction added successfully")));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(Map.of("error", "Failed to add transaction")));
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setupResponse(resp);
        req.setCharacterEncoding("UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            String idParam = req.getParameter("id");
            if (idParam == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "id parameter is required")));
                return;
            }

            Transaction t = parseTransactionFromRequest(req);
            t.setId(Integer.parseInt(idParam));

            boolean success = transactionDAO.updateTransaction(t);
            if (success) {
                out.print(gson.toJson(Map.of("message", "Transaction updated successfully")));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(Map.of("error", "Failed to update transaction")));
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setupResponse(resp);

        try (PrintWriter out = resp.getWriter()) {
            String idParam = req.getParameter("id");
            if (idParam == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "id parameter is required")));
                return;
            }

            boolean success = transactionDAO.deleteTransaction(Integer.parseInt(idParam));
            if (success) {
                out.print(gson.toJson(Map.of("message", "Transaction deleted successfully")));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Transaction not found")));
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin",  "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private Transaction parseTransactionFromRequest(HttpServletRequest req) {
        Transaction t = new Transaction();
        t.setDate(LocalDate.parse(req.getParameter("date")));      
        t.setType(req.getParameter("type"));
        t.setCategory(req.getParameter("category"));
        t.setAmount(new BigDecimal(req.getParameter("amount")));
        t.setDescription(req.getParameter("description"));
        return t;
    }

    private List<Map<String, Object>> serializeTransactions(List<Transaction> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction t : list) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",          t.getId());
            map.put("date",        t.getDate().toString());
            map.put("type",        t.getType());
            map.put("category",    t.getCategory());
            map.put("amount",      t.getAmount());
            map.put("description", t.getDescription());
            result.add(map);
        }
        return result;
    }
}

package servlet;

import com.google.gson.Gson;
import dao.BudgetDAO;
import model.Budget;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

@WebServlet("/budget/*")
public class BudgetServlet extends HttpServlet {

    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final Gson      gson      = new Gson();

    private void setup(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin",  "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setup(resp);
        String path = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {

            if ("/all".equals(path)) {

                List<Budget> budgets = budgetDAO.getAllBudgets();
                out.print(gson.toJson(serializeBudgets(budgets, false)));

            } else if ("/status".equals(path)) {

                List<Budget> budgets = budgetDAO.getBudgetVsSpending();
                out.print(gson.toJson(serializeBudgets(budgets, true)));

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Unknown budget endpoint: " + path)));
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setup(resp);
        try (PrintWriter out = resp.getWriter()) {
            Budget b = parseFromRequest(req);
            int newId = budgetDAO.addBudget(b);

            if (newId > 0) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.print(gson.toJson(Map.of("message", "Budget added", "id", newId)));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(Map.of("error", "Failed to add budget")));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setup(resp);
        try (PrintWriter out = resp.getWriter()) {
            String idParam = req.getParameter("id");
            if (idParam == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "id is required")));
                return;
            }
            Budget b = parseFromRequest(req);
            b.setId(Integer.parseInt(idParam));

            boolean ok = budgetDAO.updateBudget(b);
            out.print(gson.toJson(ok
                ? Map.of("message", "Budget updated")
                : Map.of("error",   "Budget not found or update failed")));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setup(resp);
        try (PrintWriter out = resp.getWriter()) {
            String idParam = req.getParameter("id");
            if (idParam == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "id is required")));
                return;
            }
            boolean ok = budgetDAO.deleteBudget(Integer.parseInt(idParam));
            out.print(gson.toJson(ok
                ? Map.of("message", "Budget deleted")
                : Map.of("error",   "Budget not found")));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setup(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private Budget parseFromRequest(HttpServletRequest req) {
        Budget b = new Budget();
        b.setCategory(req.getParameter("category"));
        b.setMonthlyLimit(new BigDecimal(req.getParameter("monthly_limit")));
        return b;
    }

    private List<Map<String, Object>> serializeBudgets(List<Budget> list, boolean withStatus) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Budget b : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",           b.getId());
            m.put("category",     b.getCategory());
            m.put("monthlyLimit", b.getMonthlyLimit());

            if (withStatus) {
                m.put("spent",       b.getSpent());
                m.put("remaining",   b.getRemaining());
                m.put("pctUsed",     b.getPctUsed());
                m.put("overBudget",  b.isOverBudget());
            }
            result.add(m);
        }
        return result;
    }
}

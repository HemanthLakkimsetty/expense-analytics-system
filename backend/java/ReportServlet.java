package servlet;

import com.google.gson.Gson;
import dao.ReportDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@WebServlet("/report/*")
public class ReportServlet extends HttpServlet {

    private final ReportDAO reportDAO = new ReportDAO();
    private final Gson      gson      = new Gson();

    private void setup(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin",  "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setup(resp);
        String path = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {

            if ("/months".equals(path)) {

                List<String> months = reportDAO.getAvailableMonths();
                out.print(gson.toJson(months));

            } else if ("/monthly".equals(path)) {

                String month = req.getParameter("m");
                if (month == null || month.isEmpty()) {
                    month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                }
                Map<String, Object> report = reportDAO.getMonthlyReport(month);
                out.print(gson.toJson(report));

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Unknown report endpoint: " + path)));
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setup(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}

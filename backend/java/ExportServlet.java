package servlet;

import dao.TransactionDAO;
import model.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/export/*")
public class ExportServlet extends HttpServlet {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();   

        if ("/csv".equals(path)) {
            exportCsv(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("Unknown export format: " + path);
        }
    }

    private void exportCsv(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String type     = req.getParameter("type");
        String category = req.getParameter("category");

        List<Transaction> transactions =
                transactionDAO.getFilteredTransactions(type, category);

        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"transactions_export.csv\"");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        PrintWriter out = resp.getWriter();

        out.println("ID,Date,Type,Category,Amount,Description");

        for (Transaction t : transactions) {
            out.printf("%d,%s,%s,%s,%.2f,\"%s\"%n",
                t.getId(),
                t.getDate().toString(),
                t.getType(),
                t.getCategory(),
                t.getAmount(),
                escapeCSV(t.getDescription())
            );
        }

        out.flush();
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}

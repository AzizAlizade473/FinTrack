package com.financetracker.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.financetracker.abstract_base.Transaction;
import com.financetracker.model.*;
import com.financetracker.report.MonthlySummaryReport;
import com.financetracker.report.CategoryReport;
import com.financetracker.service.FinanceService;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP server using Java's built-in com.sun.net.httpserver.
 * Provides REST API endpoints for the Personal Finance Tracker.
 * All responses include CORS headers so the React frontend can connect.
 */
public class FinanceServer {
    /** The HTTP server instance */
    private HttpServer server;
    /** The finance service that handles business logic */
    private FinanceService financeService;
    /** Server port */
    private int port;

    /**
     * Constructs a new FinanceServer.
     * @param financeService the service layer to use
     * @param port the port to bind to
     * @throws IOException if server fails to start
     */
    public FinanceServer(FinanceService financeService, int port) throws IOException {
        this.financeService = financeService;
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        registerRoutes();
    }

    /**
     * Registers all API route handlers on the server.
     */
    private void registerRoutes() {
        server.createContext("/api/auth/register", new RegisterHandler());
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/transactions/income", new AddIncomeHandler());
        server.createContext("/api/transactions/expense", new AddExpenseHandler());
        server.createContext("/api/transactions", new TransactionsHandler());
        server.createContext("/api/budgets", new BudgetsHandler());
        server.createContext("/api/reports/monthly", new MonthlyReportHandler());
        server.createContext("/api/reports/category", new CategoryReportHandler());
        server.createContext("/api/balance", new BalanceHandler());
        server.createContext("/api/alerts", new AlertsHandler());
        server.createContext("/health", new HealthHandler());
    }

    /**
     * Starts the HTTP server.
     */
    public void start() {
        server.setExecutor(null); // Use default executor
        server.start();
        System.out.println("Finance Tracker API Server running on port " + port);
    }

    // ====================== CORS HELPER ======================

    /**
     * Adds CORS headers to EVERY response so React frontend can connect.
     * @param exchange the HTTP exchange to add headers to
     */
    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
    }

    /**
     * Handles CORS preflight OPTIONS requests.
     * @param exchange the HTTP exchange
     * @return true if this was an OPTIONS request (already handled)
     */
    private static boolean handleCorsPreFlight(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // No content
            return true;
        }
        return false;
    }

    /**
     * Sends a JSON response with the given status code and body.
     * CORS headers are added to every response.
     * @param exchange the HTTP exchange
     * @param statusCode HTTP status code
     * @param jsonResponse the JSON response body
     */
    private static void sendResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        addCorsHeaders(exchange);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    /**
     * Reads the request body as a string from the InputStream.
     * @param exchange the HTTP exchange
     * @return the request body as a string
     */
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * Extracts query parameter value from the request URI.
     * @param exchange the HTTP exchange
     * @param paramName the parameter name to look for
     * @return the parameter value, or null if not found
     */
    private static String getQueryParam(HttpExchange exchange, String paramName) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && pair[0].equals(paramName)) {
                return pair[1];
            }
        }
        return null;
    }

    // ====================== ROUTE HANDLERS ======================

    /**
     * POST /api/auth/register — Registers a new user.
     */
    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreFlight(exchange)) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, JsonHelper.errorResponse("Method not allowed"));
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> data = JsonHelper.parseJson(body);

            String name = data.get("name");
            String email = data.get("email");
            String password = data.get("password");

            // Validate input
            if (name == null || email == null || password == null
                    || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                sendResponse(exchange, 400, JsonHelper.errorResponse("Name, email, and password are required"));
                return;
            }

            User user = financeService.registerUser(name, email, password);
            if (user == null) {
                sendResponse(exchange, 409, JsonHelper.errorResponse("Email already registered"));
                return;
            }

            String userJson = JsonHelper.userToJson(user.getUserId(), user.getName(), user.getEmail());
            sendResponse(exchange, 201, JsonHelper.successResponse(userJson));
        }
    }

    /**
     * POST /api/auth/login — Authenticates a user.
     */
    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreFlight(exchange)) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, JsonHelper.errorResponse("Method not allowed"));
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> data = JsonHelper.parseJson(body);

            String email = data.get("email");
            String password = data.get("password");

            if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
                sendResponse(exchange, 400, JsonHelper.errorResponse("Email and password are required"));
                return;
            }

            User user = financeService.loginUser(email, password);
            if (user == null) {
                sendResponse(exchange, 401, JsonHelper.errorResponse("Invalid email or password"));
                return;
            }

            String userJson = JsonHelper.userToJson(user.getUserId(), user.getName(), user.getEmail());
            sendResponse(exchange, 200, JsonHelper.successResponse(userJson));
        }
    }

    /**
     * GET /api/transactions — Get all transactions.
     * DELETE /api/transactions/{id} — Delete a transaction by id.
     */
    private class TransactionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreFlight(exchange)) return;

            String method = exchange.getRequestMethod().toUpperCase();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method)) {
                // Return all transactions
                List<Transaction> transactions = financeService.getAllTransactions();
                List<String> jsonList = new ArrayList<>();
                for (Transaction t : transactions) {
                    String category = "";
                    String source = "";
                    if (t instanceof Income) {
                        category = ((Income) t).getCategory();
                        source = ((Income) t).getSource();
                    } else if (t instanceof Expense) {
                        category = ((Expense) t).getCategory();
                    }
                    jsonList.add(JsonHelper.transactionToJson(
                            t.getId(), t.getType(), t.getAmount(),
                            t.getDate(), t.getDescription(), category, source));
                }
                sendResponse(exchange, 200, JsonHelper.successResponse(JsonHelper.toJsonArray(jsonList)));

            } else if ("DELETE".equals(method)) {
                // Extract transaction ID from path: /api/transactions/{id}
                String id = path.substring("/api/transactions/".length());
                if (id.isEmpty()) {
                    sendResponse(exchange, 400, JsonHelper.errorResponse("Transaction ID is required"));
                    return;
                }
                boolean deleted = financeService.deleteTransaction(id);
                if (deleted) {
                    sendResponse(exchange, 200, JsonHelper.successResponse("{\"deleted\":true}"));
                } else {
                    sendResponse(exchange, 404, JsonHelper.errorResponse("Transaction not found"));
                }

            } else {
                sendResponse(exchange, 405, JsonHelper.errorResponse("Method not allowed"));
            }
        }
    }

    /**
     * POST /api/transactions/income — Add a new income transaction.
     */
    private class AddIncomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreFlight(exchange)) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, JsonHelper.errorResponse("Method not allowed"));
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> data = JsonHelper.parseJson(body);

            String desc = data.get("description");
            String amountStr = data.get("amount");
            String date = data.get("date");
            String category = data.get("category");
            String source = data.get("source");

            // Validate required fields
            if (desc == null || amountStr == null || date == null) {
                sendResponse(exchange, 400, JsonHelper.errorResponse("Description, amount, and date are required"));
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, JsonHelper.errorResponse("Invalid amount"));
                return;
            }

            if (category == null) category = "General";
            if (source == null) source = "";

            Income income = financeService.addIncome(desc, amount, date, category, source);
            String json = JsonHelper.transactionToJson(income.getId(), income.getType(),
                    income.getAmount(), income.getDate(), income.getDescription(),
                    income.getCategory(), income.getSource());
            sendResponse(exchange, 201, JsonHelper.successResponse(json));
        }
    }

    /**
     * POST /api/transactions/expense — Add a new expense transaction.
     */
    private class AddExpenseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreFlight(exchange)) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, JsonHelper.errorResponse("Method not allowed"));
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> data = JsonHelper.parseJson(body);

            String desc = data.get("description");
            String amountStr = data.get("amount");
            String date = data.get("date");
            String category = data.get("category");

            if (desc == null || amountStr == null || date == null) {
                sendResponse(exchange, 400, JsonHelper.errorResponse("Description, amount, and date are required"));
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, JsonHelper.errorResponse("Invalid amount"));
                return;
            }

            if (category == null) category = "General";

            Expense expense = financeService.addExpense(desc, amount, date, category);
            String json = JsonHelper.transactionToJson(expense.getId(), expense.getType(),
                    expense.getAmount(), expense.getDate(), expense.getDescription(),
                    expense.getCategory(), "");
            sendResponse(exchange, 201, JsonHelper.successResponse(json));
        }
    }

    /**
     * GET /api/budgets — Get all budgets.
     * POST /api/budgets — Set/update a budget.
     */
    private class BudgetsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreFlight(exchange)) return;

            String method = exchange.getRequestMethod().toUpperCase();

            if ("GET".equals(method)) {
                // Return all budgets
                List<Budget> budgets = financeService.getAllBudgets();
                List<String> jsonList = new ArrayList<>();
                for (Budget b : budgets) {
                    jsonList.add(JsonHelper.budgetToJson(b.getCategory(), b.getLimit(),
                            b.getSpent(), b.isExceeded(), b.getRemainingBudget()));
                }
                sendResponse(exchange, 200, JsonHelper.successResponse(JsonHelper.toJsonArray(jsonList)));

            } else if ("POST".equals(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> data = JsonHelper.parseJson(body);

                String category = data.get("category");
                String limitStr = data.get("limit");

                if (category == null || limitStr == null || category.isEmpty()) {
                    sendResponse(exchange, 400, JsonHelper.errorResponse("Category and limit are required"));
                    return;
                }

                double limit;
                try {
                    limit = Double.parseDouble(limitStr);
                } catch (NumberFormatException e) {
                    sendResponse(exchange, 400, JsonHelper.errorResponse("Invalid limit amount"));
                    return;
                }

                Budget budget = financeService.setBudget(category, limit);
                String json = JsonHelper.budgetToJson(budget.getCategory(), budget.getLimit(),
                        budget.getSpent(), budget.isExceeded(), budget.getRemainingBudget());
                sendResponse(exchange, 201, JsonHelper.successResponse(json));

            } else {
                sendResponse(exchange, 405, JsonHelper.errorResponse("Method not allowed"));
            }
        }
    }

    /**
     * GET /api/reports/monthly?month=2024-03 — Generate monthly summary report.
     */
    private class MonthlyReportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreFlight(exchange)) return;

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, JsonHelper.errorResponse("Method not allowed"));
                return;
            }

            String month = getQueryParam(exchange, "month");
            if (month == null || month.isEmpty()) {
                sendResponse(exchange, 400, JsonHelper.errorResponse("Month parameter is required (e.g., ?month=2024-03)"));
                return;
            }

            MonthlySummaryReport report = financeService.generateMonthlyReport(month);
            double totalIncome = report.getTotalIncome();
            double totalExpense = report.getTotalExpense();
            double netBalance = totalIncome - totalExpense;

            String json = "{\"title\":\"" + JsonHelper.escapeJson(report.getTitle()) + "\","
                    + "\"month\":\"" + JsonHelper.escapeJson(month) + "\","
                    + "\"totalIncome\":" + totalIncome + ","
                    + "\"totalExpense\":" + totalExpense + ","
                    + "\"netBalance\":" + netBalance + ","
                    + "\"report\":\"" + JsonHelper.escapeJson(report.generate()) + "\"}";

            sendResponse(exchange, 200, JsonHelper.successResponse(json));
        }
    }

    /**
     * GET /api/reports/category — Generate category breakdown report.
     */
    private class CategoryReportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreFlight(exchange)) return;

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, JsonHelper.errorResponse("Method not allowed"));
                return;
            }

            CategoryReport report = financeService.generateCategoryReport();
            Map<String, Double> breakdown = report.getCategoryBreakdown();

            // Calculate total for percentage
            double totalSpending = 0;
            for (double val : breakdown.values()) {
                totalSpending += val;
            }

            // Build category items array
            List<String> items = new ArrayList<>();
            for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
                double pct = totalSpending > 0 ? (entry.getValue() / totalSpending) * 100 : 0;
                items.add("{\"category\":\"" + JsonHelper.escapeJson(entry.getKey()) + "\","
                        + "\"total\":" + entry.getValue() + ","
                        + "\"percentage\":" + Math.round(pct * 10.0) / 10.0 + "}");
            }

            String json = "{\"title\":\"" + JsonHelper.escapeJson(report.getTitle()) + "\","
                    + "\"totalSpending\":" + totalSpending + ","
                    + "\"categories\":" + JsonHelper.toJsonArray(items) + ","
                    + "\"report\":\"" + JsonHelper.escapeJson(report.generate()) + "\"}";

            sendResponse(exchange, 200, JsonHelper.successResponse(json));
        }
    }

    /**
     * GET /api/balance — Get current balance.
     */
    private class BalanceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreFlight(exchange)) return;

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, JsonHelper.errorResponse("Method not allowed"));
                return;
            }

            double balance = financeService.getBalance();
            String json = "{\"balance\":" + balance + "}";
            sendResponse(exchange, 200, JsonHelper.successResponse(json));
        }
    }

    /**
     * GET /api/alerts — Get budget-exceeded alerts.
     */
    private class AlertsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreFlight(exchange)) return;

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, JsonHelper.errorResponse("Method not allowed"));
                return;
            }

            List<String> alerts = financeService.getAlerts();
            sendResponse(exchange, 200, JsonHelper.successResponse(JsonHelper.stringsToJsonArray(alerts)));
        }
    }

    /**
     * GET /health — Health check endpoint for Railway.
     */
    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCorsPreFlight(exchange)) return;
            
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, JsonHelper.errorResponse("Method not allowed"));
                return;
            }
            sendResponse(exchange, 200, "{\"status\":\"ok\"}");
        }
    }
}

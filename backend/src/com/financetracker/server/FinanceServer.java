package com.financetracker.server;

import com.sun.net.httpserver.HttpServer;
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

public class FinanceServer {
    private HttpServer server;
    private FinanceService financeService;
    private int port;

    public FinanceServer(FinanceService financeService, int port) throws IOException {
        this.financeService = financeService;
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        registerRoutes();
    }

    private void registerRoutes() {
        server.createContext("/api/auth/register", exchange -> {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleRegister(exchange);
        });

        server.createContext("/api/auth/login", exchange -> {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleLogin(exchange);
        });

        server.createContext("/api/transactions/income", exchange -> {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleAddIncome(exchange);
        });

        server.createContext("/api/transactions/expense", exchange -> {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleAddExpense(exchange);
        });

        server.createContext("/api/transactions", exchange -> {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String method = exchange.getRequestMethod().toUpperCase();
            if (method.equals("GET") || method.equals("DELETE")) {
                handleTransactions(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
        });

        server.createContext("/api/budgets", exchange -> {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String method = exchange.getRequestMethod().toUpperCase();
            if (method.equals("GET") || method.equals("POST")) {
                handleBudgets(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
        });

        server.createContext("/api/reports/monthly", exchange -> {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleMonthlyReport(exchange);
        });

        server.createContext("/api/reports/category", exchange -> {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleCategoryReport(exchange);
        });

        server.createContext("/api/balance", exchange -> {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleBalance(exchange);
        });

        server.createContext("/api/alerts", exchange -> {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleAlerts(exchange);
        });

        server.createContext("/health", exchange -> {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleHealth(exchange);
        });
    }

    public void start() {
        server.setExecutor(null);
        server.start();
        System.out.println("Finance Tracker API Server running on port " + port);
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toString(StandardCharsets.UTF_8.name());
    }

    private String getQueryParam(HttpExchange exchange, String paramName) {
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

    // --- Handlers ---

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> data = JsonHelper.parseJson(body);
        String name = data.get("name");
        String email = data.get("email");
        String password = data.get("password");

        if (name == null || email == null || password == null || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
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

    private void handleLogin(HttpExchange exchange) throws IOException {
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

    private void handleTransactions(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equals(method)) {
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
                jsonList.add(JsonHelper.transactionToJson(t.getId(), t.getType(), t.getAmount(), t.getDate(), t.getDescription(), category, source));
            }
            sendResponse(exchange, 200, JsonHelper.successResponse(JsonHelper.toJsonArray(jsonList)));

        } else if ("DELETE".equals(method)) {
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
        }
    }

    private void handleAddIncome(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> data = JsonHelper.parseJson(body);

        String desc = data.get("description");
        String amountStr = data.get("amount");
        String date = data.get("date");
        String category = data.get("category");
        String source = data.get("source");

        if (desc == null || amountStr == null || date == null) {
            sendResponse(exchange, 400, JsonHelper.errorResponse("Description, amount, and date are required"));
            return;
        }

        double amount;
        try { amount = Double.parseDouble(amountStr); } catch (NumberFormatException e) {
            sendResponse(exchange, 400, JsonHelper.errorResponse("Invalid amount"));
            return;
        }

        if (category == null) category = "General";
        if (source == null) source = "";

        Income income = financeService.addIncome(desc, amount, date, category, source);
        String json = JsonHelper.transactionToJson(income.getId(), income.getType(), income.getAmount(), income.getDate(), income.getDescription(), income.getCategory(), income.getSource());
        sendResponse(exchange, 201, JsonHelper.successResponse(json));
    }

    private void handleAddExpense(HttpExchange exchange) throws IOException {
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
        try { amount = Double.parseDouble(amountStr); } catch (NumberFormatException e) {
            sendResponse(exchange, 400, JsonHelper.errorResponse("Invalid amount"));
            return;
        }

        if (category == null) category = "General";

        Expense expense = financeService.addExpense(desc, amount, date, category);
        String json = JsonHelper.transactionToJson(expense.getId(), expense.getType(), expense.getAmount(), expense.getDate(), expense.getDescription(), expense.getCategory(), "");
        sendResponse(exchange, 201, JsonHelper.successResponse(json));
    }

    private void handleBudgets(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("GET".equals(method)) {
            List<Budget> budgets = financeService.getAllBudgets();
            List<String> jsonList = new ArrayList<>();
            for (Budget b : budgets) {
                jsonList.add(JsonHelper.budgetToJson(b.getCategory(), b.getLimit(), b.getSpent(), b.isExceeded(), b.getRemainingBudget()));
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
            try { limit = Double.parseDouble(limitStr); } catch (NumberFormatException e) {
                sendResponse(exchange, 400, JsonHelper.errorResponse("Invalid limit amount"));
                return;
            }

            Budget budget = financeService.setBudget(category, limit);
            String json = JsonHelper.budgetToJson(budget.getCategory(), budget.getLimit(), budget.getSpent(), budget.isExceeded(), budget.getRemainingBudget());
            sendResponse(exchange, 201, JsonHelper.successResponse(json));
        }
    }

    private void handleMonthlyReport(HttpExchange exchange) throws IOException {
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

    private void handleCategoryReport(HttpExchange exchange) throws IOException {
        CategoryReport report = financeService.generateCategoryReport();
        Map<String, Double> breakdown = report.getCategoryBreakdown();

        double totalSpending = 0;
        for (double val : breakdown.values()) totalSpending += val;

        List<String> items = new ArrayList<>();
        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            double pct = totalSpending > 0 ? (entry.getValue() / totalSpending) * 100 : 0;
            items.add("{\"category\":\"" + JsonHelper.escapeJson(entry.getKey()) + "\",\"total\":" + entry.getValue() + ",\"percentage\":" + Math.round(pct * 10.0) / 10.0 + "}");
        }

        String json = "{\"title\":\"" + JsonHelper.escapeJson(report.getTitle()) + "\",\"totalSpending\":" + totalSpending + ",\"categories\":" + JsonHelper.toJsonArray(items) + ",\"report\":\"" + JsonHelper.escapeJson(report.generate()) + "\"}";
        sendResponse(exchange, 200, JsonHelper.successResponse(json));
    }

    private void handleBalance(HttpExchange exchange) throws IOException {
        double balance = financeService.getBalance();
        String json = "{\"balance\":" + balance + "}";
        sendResponse(exchange, 200, JsonHelper.successResponse(json));
    }

    private void handleAlerts(HttpExchange exchange) throws IOException {
        List<String> alerts = financeService.getAlerts();
        sendResponse(exchange, 200, JsonHelper.successResponse(JsonHelper.stringsToJsonArray(alerts)));
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 200, "{\"status\":\"ok\"}");
    }
}

package com.financetracker.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.financetracker.service.FinanceService;
import java.io.*;
import java.net.InetSocketAddress;

public class FinanceServer {

    private final FinanceService financeService;
    private final int port;

    public FinanceServer(FinanceService financeService, int port) {
        this.financeService = financeService;
        this.port = port;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/health",                    this::handleHealth);
        server.createContext("/api/auth/register",         this::handleRegister);
        server.createContext("/api/auth/login",            this::handleLogin);
        server.createContext("/api/balance",               this::handleBalance);
        server.createContext("/api/alerts",                this::handleAlerts);
        server.createContext("/api/budgets",               this::handleBudgets);
        server.createContext("/api/reports/monthly",       this::handleMonthlyReport);
        server.createContext("/api/reports/category",      this::handleCategoryReport);
        server.createContext("/api/transactions/income",   this::handleAddIncome);
        server.createContext("/api/transactions/expense",  this::handleAddExpense);
        server.createContext("/api/transactions",          this::handleTransactions);

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + port);
    }

    private void addCors(HttpExchange e) {
        e.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        e.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        e.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private boolean preflight(HttpExchange e) throws IOException {
        addCors(e);
        if (e.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            e.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private void json(HttpExchange e, int code, String body) throws IOException {
        addCors(e);
        e.getResponseHeaders().set("Content-Type", "application/json");
        byte[] b = body.getBytes("UTF-8");
        e.sendResponseHeaders(code, b.length);
        e.getResponseBody().write(b);
        e.getResponseBody().close();
    }

    private String body(HttpExchange e) throws IOException {
        return new String(e.getRequestBody().readAllBytes(), "UTF-8");
    }

    private String extract(String json, String key) {
        String search = "\"" + key + "\"";
        int i = json.indexOf(search);
        if (i == -1) return "";
        int colon = json.indexOf(":", i);
        if (colon == -1) return "";
        // Skip whitespace after colon
        int pos = colon + 1;
        while (pos < json.length() && json.charAt(pos) == ' ') pos++;
        if (pos >= json.length()) return "";
        // Check if value is quoted string or unquoted (number/boolean)
        if (json.charAt(pos) == '"') {
            int quote2 = json.indexOf("\"", pos + 1);
            if (quote2 == -1) return "";
            return json.substring(pos + 1, quote2);
        } else {
            // Unquoted value (number, boolean, null)
            int end = pos;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']') {
                end++;
            }
            return json.substring(pos, end).trim();
        }
    }

    // ============ HANDLERS ============

    private void handleHealth(HttpExchange e) throws IOException {
        if (preflight(e)) return;
        json(e, 200, "{\"status\":\"ok\"}");
    }

    private void handleRegister(HttpExchange e) throws IOException {
        if (preflight(e)) return;
        if (!e.getRequestMethod().equalsIgnoreCase("POST")) { json(e, 405, "{\"error\":\"method not allowed\"}"); return; }
        String b = body(e);
        String name = extract(b, "name");
        String email = extract(b, "email");
        String password = extract(b, "password");
        System.out.println("Register attempt: " + email);
        boolean ok = financeService.registerUser(name, email, password);
        if (ok) json(e, 200, "{\"status\":\"success\",\"message\":\"Registered\"}");
        else json(e, 400, "{\"status\":\"error\",\"message\":\"Email already exists\"}");
    }

    private void handleLogin(HttpExchange e) throws IOException {
        if (preflight(e)) return;
        if (!e.getRequestMethod().equalsIgnoreCase("POST")) { json(e, 405, "{\"error\":\"method not allowed\"}"); return; }
        String b = body(e);
        String email = extract(b, "email");
        String password = extract(b, "password");
        System.out.println("Login attempt: " + email);
        String user = financeService.loginUser(email, password);
        if (user != null) json(e, 200, "{\"status\":\"success\",\"user\":" + user + "}");
        else json(e, 401, "{\"status\":\"error\",\"message\":\"Invalid credentials\"}");
    }

    private void handleBalance(HttpExchange e) throws IOException {
        if (preflight(e)) return;
        double balance = financeService.getBalance();
        double totalIncome = financeService.getTotalIncome();
        double totalExpense = financeService.getTotalExpense();
        json(e, 200, "{\"balance\":" + balance + ",\"totalIncome\":" + totalIncome + ",\"totalExpense\":" + totalExpense + "}");
    }

    private void handleAlerts(HttpExchange e) throws IOException {
        if (preflight(e)) return;
        json(e, 200, financeService.getAlertsJson());
    }

    private void handleBudgets(HttpExchange e) throws IOException {
        if (preflight(e)) return;
        if (e.getRequestMethod().equalsIgnoreCase("GET")) {
            json(e, 200, financeService.getBudgetsJson());
        } else if (e.getRequestMethod().equalsIgnoreCase("POST")) {
            String b = body(e);
            String category = extract(b, "category");
            String limitStr = extract(b, "limit");
            double limit = 0;
            try { limit = Double.parseDouble(limitStr); } catch (Exception ex) { /* default 0 */ }
            financeService.setBudget(category, limit);
            json(e, 200, "{\"status\":\"success\"}");
        }
    }

    private void handleTransactions(HttpExchange e) throws IOException {
        if (preflight(e)) return;
        if (e.getRequestMethod().equalsIgnoreCase("GET")) {
            json(e, 200, financeService.getTransactionsJson());
        } else if (e.getRequestMethod().equalsIgnoreCase("DELETE")) {
            String path = e.getRequestURI().getPath();
            String id = path.substring(path.lastIndexOf("/") + 1);
            financeService.deleteTransaction(id);
            json(e, 200, "{\"status\":\"success\"}");
        }
    }

    private void handleAddIncome(HttpExchange e) throws IOException {
        if (preflight(e)) return;
        if (!e.getRequestMethod().equalsIgnoreCase("POST")) { json(e, 405, "{\"error\":\"method not allowed\"}"); return; }
        String b = body(e);
        double amount = 0;
        try { amount = Double.parseDouble(extract(b, "amount")); } catch (Exception ex) { /* default 0 */ }
        financeService.addIncome(
            extract(b, "description"),
            amount,
            extract(b, "date"),
            extract(b, "category"),
            extract(b, "source")
        );
        json(e, 200, "{\"status\":\"success\"}");
    }

    private void handleAddExpense(HttpExchange e) throws IOException {
        if (preflight(e)) return;
        if (!e.getRequestMethod().equalsIgnoreCase("POST")) { json(e, 405, "{\"error\":\"method not allowed\"}"); return; }
        String b = body(e);
        double amount = 0;
        try { amount = Double.parseDouble(extract(b, "amount")); } catch (Exception ex) { /* default 0 */ }
        financeService.addExpense(
            extract(b, "description"),
            amount,
            extract(b, "date"),
            extract(b, "category")
        );
        json(e, 200, "{\"status\":\"success\"}");
    }

    private void handleMonthlyReport(HttpExchange e) throws IOException {
        if (preflight(e)) return;
        String query = e.getRequestURI().getQuery();
        String month = "";
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("month=")) {
                    month = param.substring(6);
                }
            }
        }
        json(e, 200, financeService.getMonthlyReportJson(month));
    }

    private void handleCategoryReport(HttpExchange e) throws IOException {
        if (preflight(e)) return;
        json(e, 200, financeService.getCategoryReportJson());
    }
}

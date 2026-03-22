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

        server.createContext("/health",                    this::handleHealth).getFilters().add(corsFilter());
        server.createContext("/api/auth/register",         this::handleRegister).getFilters().add(corsFilter());
        server.createContext("/api/auth/login",            this::handleLogin).getFilters().add(corsFilter());
        server.createContext("/api/balance",               this::handleBalance).getFilters().add(corsFilter());
        server.createContext("/api/alerts",                this::handleAlerts).getFilters().add(corsFilter());
        server.createContext("/api/budgets",               this::handleBudgets).getFilters().add(corsFilter());
        server.createContext("/api/reports/monthly",       this::handleMonthlyReport).getFilters().add(corsFilter());
        server.createContext("/api/reports/category",      this::handleCategoryReport).getFilters().add(corsFilter());
        server.createContext("/api/transactions/income",   this::handleAddIncome).getFilters().add(corsFilter());
        server.createContext("/api/transactions/expense",  this::handleAddExpense).getFilters().add(corsFilter());
        server.createContext("/api/transactions",          this::handleTransactions).getFilters().add(corsFilter());

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + port);
    }

    private com.sun.net.httpserver.Filter corsFilter() {
        return new com.sun.net.httpserver.Filter() {
            public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                chain.doFilter(exchange);
            }
            public String description() { return "CORS"; }
        };
    }

    private void json(HttpExchange e, int code, String body) throws IOException {
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
        int quote1 = json.indexOf("\"", colon);
        int quote2 = json.indexOf("\"", quote1 + 1);
        return json.substring(quote1 + 1, quote2);
    }

    private void handleHealth(HttpExchange e) throws IOException {
        json(e, 200, "{\"status\":\"ok\"}");
    }

    private void handleRegister(HttpExchange e) throws IOException {
        if (!e.getRequestMethod().equalsIgnoreCase("POST")) { json(e, 405, "{\"error\":\"method not allowed\"}"); return; }
        String b = body(e);
        String name = extract(b, "name");
        String email = extract(b, "email");
        String password = extract(b, "password");
        boolean ok = financeService.registerUser(name, email, password);
        if (ok) json(e, 200, "{\"status\":\"success\",\"message\":\"Registered\"}");
        else json(e, 400, "{\"status\":\"error\",\"message\":\"Email already exists\"}");
    }

    private void handleLogin(HttpExchange e) throws IOException {
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
        double balance = financeService.getBalance();
        json(e, 200, "{\"balance\":" + balance + "}");
    }

    private void handleAlerts(HttpExchange e) throws IOException {
        json(e, 200, financeService.getAlertsJson());
    }

    private void handleBudgets(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equalsIgnoreCase("GET")) {
            json(e, 200, financeService.getBudgetsJson());
        } else if (e.getRequestMethod().equalsIgnoreCase("POST")) {
            String b = body(e);
            String category = extract(b, "category");
            double limit = Double.parseDouble(extract(b, "limit"));
            financeService.setBudget(category, limit);
            json(e, 200, "{\"status\":\"success\"}");
        }
    }

    private void handleTransactions(HttpExchange e) throws IOException {
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
        if (!e.getRequestMethod().equalsIgnoreCase("POST")) { json(e, 405, "{\"error\":\"method not allowed\"}"); return; }
        String b = body(e);
        financeService.addIncome(
            extract(b, "description"),
            Double.parseDouble(extract(b, "amount")),
            extract(b, "date"),
            extract(b, "category"),
            extract(b, "source")
        );
        json(e, 200, "{\"status\":\"success\"}");
    }

    private void handleAddExpense(HttpExchange e) throws IOException {
        if (!e.getRequestMethod().equalsIgnoreCase("POST")) { json(e, 405, "{\"error\":\"method not allowed\"}"); return; }
        String b = body(e);
        financeService.addExpense(
            extract(b, "description"),
            Double.parseDouble(extract(b, "amount")),
            extract(b, "date"),
            extract(b, "category")
        );
        json(e, 200, "{\"status\":\"success\"}");
    }

    private void handleMonthlyReport(HttpExchange e) throws IOException {
        String query = e.getRequestURI().getQuery();
        String month = (query != null && query.contains("month=")) ? query.split("month=")[1] : "";
        json(e, 200, financeService.getMonthlyReportJson(month));
    }

    private void handleCategoryReport(HttpExchange e) throws IOException {
        json(e, 200, financeService.getCategoryReportJson());
    }
}

package com.financetracker.main;

/*
 * ============================================================
 * UML RELATIONSHIPS:
 * ============================================================
 * Transaction (abstract) <|-- Income
 * Transaction (abstract) <|-- Expense
 * Income ..|> Trackable, Categorizable
 * Expense ..|> Trackable, Categorizable
 * Budget ..|> Trackable
 * Report (abstract) <|-- MonthlySummaryReport
 * Report (abstract) <|-- CategoryReport
 * MonthlySummaryReport ..|> Exportable
 * CategoryReport ..|> Exportable
 * User "1" *-- "many" Transaction  (Composition)
 * FinanceService --> User
 * FinanceService --> FinanceManager
 * FinanceManager --> FinanceObserver (Observer Pattern)
 * BudgetAlertObserver ..|> FinanceObserver
 * ============================================================
 */

import com.financetracker.service.FinanceService;
import com.financetracker.server.FinanceServer;

/**
 * Main entry point for the Personal Finance Tracker application.
 * Initializes the finance service with sample data and starts the HTTP server.
 */
public class Main {

    /**
     * Application entry point.
     * Loads sample data (or previously saved data) and starts the REST API server on port 8080.
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   Personal Finance Tracker v1.0");
        System.out.println("========================================");

        try {
            // Initialize the finance service
            FinanceService financeService = new FinanceService();

            // Load saved data, or initialize empty state
            financeService.initializeData();

            // Start the HTTP server
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            FinanceServer server = new FinanceServer(financeService, port);
            server.start();

            System.out.println("========================================");
            System.out.println("   API running at port: " + port);
            System.out.println("   Press Ctrl+C to stop");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

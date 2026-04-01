package com.financetracker.main;

import com.financetracker.service.FinanceService;
import com.financetracker.server.FinanceServer;

public class Main {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   Personal Finance Tracker v1.0");
        System.out.println("========================================");

        try {
            FinanceService financeService = new FinanceService();
            financeService.initializeData();

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

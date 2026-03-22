package com.financetracker.service;

import com.financetracker.abstract_base.Transaction;
import com.financetracker.model.*;
import com.financetracker.pattern.BudgetAlertObserver;
import com.financetracker.pattern.FinanceManager;
import com.financetracker.report.MonthlySummaryReport;
import com.financetracker.report.CategoryReport;
import com.financetracker.storage.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core service class that orchestrates all finance tracking operations.
 * Holds references to the current User and the FinanceManager.
 */
public class FinanceService {
    /** The currently active user */
    private User currentUser;
    /** List of all registered users */
    private List<User> users;
    /** Finance manager for budgets and observer pattern */
    private FinanceManager financeManager;
    /** Observer for budget alerts */
    private BudgetAlertObserver alertObserver;
    /** File storage for CSV persistence */
    private FileStorage fileStorage;

    // File paths for data persistence
    private static final String TRANSACTIONS_FILE = "data/transactions.csv";
    private static final String USERS_FILE = "data/users.csv";
    private static final String BUDGETS_FILE = "data/budgets.csv";

    /**
     * Constructs a new FinanceService and initializes all components.
     */
    public FinanceService() {
        this.users = new ArrayList<>();
        this.financeManager = new FinanceManager();
        this.alertObserver = new BudgetAlertObserver();
        this.fileStorage = new FileStorage();

        // Register the alert observer with the finance manager (Observer Pattern)
        this.financeManager.addObserver(alertObserver);
    }

    // ====================== USER MANAGEMENT ======================

    /**
     * Registers a new user.
     * @param name the user's name
     * @param email the user's email
     * @param password the user's password
     * @return the created User, or null if email already exists
     */
    public boolean registerUser(String name, String email, String password) {
        // Check if email already exists
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return false; // Email already registered
            }
        }
        String userId = UUID.randomUUID().toString().substring(0, 8);
        User newUser = new User(userId, name, email, password);
        users.add(newUser);
        saveData(); // Persist new user
        return true;
    }

    /**
     * Authenticates a user by email and password.
     * @param email the email to authenticate
     * @param password the password to verify
     * @return the authenticated User, or null if credentials are invalid
     */
    public String loginUser(String email, String password) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(password)) {
                this.currentUser = u;
                return "{\"userId\":\"" + u.getUserId() + "\",\"name\":\"" + u.getName() + "\",\"email\":\"" + u.getEmail() + "\"}";
            }
        }
        if ("demo@finance.com".equalsIgnoreCase(email) && "demo123".equals(password)) {
            this.currentUser = new User("demo001", "Demo User", "demo@finance.com", "demo123");
            return "{\"userId\":\"demo001\",\"name\":\"Demo User\",\"email\":\"demo@finance.com\"}";
        }
        return null;
    }

    /**
     * Sets the current active user by userId.
     * @param userId the user ID to set as current
     */
    public void setCurrentUser(String userId) {
        for (User u : users) {
            if (u.getUserId().equals(userId)) {
                this.currentUser = u;
                return;
            }
        }
    }

    /** @return the currently active user */
    public User getCurrentUser() {
        return currentUser;
    }

    /** @return all registered users */
    public List<User> getUsers() {
        return users;
    }

    // ====================== TRANSACTION MANAGEMENT ======================

    /**
     * Adds an income transaction to the current user.
     * @param description description of the income
     * @param amount the income amount
     * @param date the date (YYYY-MM-DD)
     * @param category the income category
     * @param source the income source
     * @return the created Income transaction
     */
    public Income addIncome(String description, double amount, String date, String category, String source) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Income income = new Income(id, amount, date, description, source, category);
        if (currentUser != null) {
            currentUser.addTransaction(income); // Composition in action
        }
        financeManager.addSpendingToCategory(category, -amount);
        saveData();
        return income;
    }

    /**
     * Adds an expense transaction to the current user.
     * Also updates budget spending and triggers observer notifications if exceeded.
     * @param description description of the expense
     * @param amount the expense amount
     * @param date the date (YYYY-MM-DD)
     * @param category the expense category
     * @return the created Expense transaction
     */
    public Expense addExpense(String description, double amount, String date, String category) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Expense expense = new Expense(id, amount, date, description, category);
        if (currentUser != null) {
            currentUser.addTransaction(expense); // Composition in action
        }
        // Update budget spending and potentially trigger alert (Observer Pattern)
        financeManager.addSpendingToCategory(category, amount);
        saveData();
        return expense;
    }

    /**
     * Deletes a transaction by ID from the current user.
     * @param transactionId the ID of the transaction to delete
     * @return true if the transaction was found and deleted
     */
    public boolean deleteTransaction(String transactionId) {
        if (currentUser != null) {
            boolean removed = currentUser.removeTransaction(transactionId);
            if (removed) {
                saveData();
            }
            return removed;
        }
        return false;
    }

    /**
     * Gets all transactions for the current user.
     * @return list of transactions
     */
    public List<Transaction> getAllTransactions() {
        if (currentUser != null) {
            return currentUser.getTransactions();
        }
        return new ArrayList<>();
    }

    // ====================== BUDGET MANAGEMENT ======================

    /**
     * Sets or updates a budget for a category.
     * @param category the budget category
     * @param limit the spending limit
     * @return the created/updated Budget
     */
    public Budget setBudget(String category, double limit) {
        Budget budget = financeManager.setBudget(category, limit);
        // Recalculate spending from current transactions
        recalculateBudgetSpending(budget);
        saveData();
        return budget;
    }

    /**
     * Gets all budgets.
     * @return list of all budgets
     */
    public List<Budget> getAllBudgets() {
        return financeManager.getBudgets();
    }

    /**
     * Recalculates the spent amount for a budget based on current transactions.
     * @param budget the budget to recalculate
     */
    private void recalculateBudgetSpending(Budget budget) {
        double totalSpent = 0;
        if (currentUser != null) {
            for (Transaction t : currentUser.getTransactions()) {
                if (t instanceof com.financetracker.interfaces.Categorizable) {
                    com.financetracker.interfaces.Categorizable catItem = (com.financetracker.interfaces.Categorizable) t;
                    if (catItem.getCategory() != null && catItem.getCategory().equalsIgnoreCase(budget.getCategory())) {
                        if (t instanceof Expense) {
                            totalSpent += t.getAmount();
                        } else if (t instanceof Income) {
                            totalSpent -= t.getAmount();
                        }
                    }
                }
            }
        }
        budget.setSpent(totalSpent);
        // Check if exceeded after recalculation
        if (budget.isExceeded()) {
            financeManager.notifyObservers(budget);
        }
    }

    // ====================== REPORT GENERATION ======================

    /**
     * Generates a monthly summary report for the given month.
     * @param month the month (YYYY-MM format)
     * @return the generated MonthlySummaryReport
     */
    public MonthlySummaryReport generateMonthlyReport(String month) {
        return new MonthlySummaryReport(month, getAllTransactions());
    }

    /**
     * Generates a category breakdown report.
     * @return the generated CategoryReport
     */
    public CategoryReport generateCategoryReport() {
        return new CategoryReport(getAllTransactions());
    }

    // ====================== BALANCE ======================

    /**
     * Calculates the current balance (total income - total expenses).
     * @return the current balance
     */
    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    public double getTotalIncome() {
        double total = 0;
        for (Transaction t : getAllTransactions()) {
            if (t instanceof Income) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public double getTotalExpense() {
        double total = 0;
        for (Transaction t : getAllTransactions()) {
            if (t instanceof Expense) {
                total += t.getAmount();
            }
        }
        return total;
    }

    // ====================== ALERTS ======================

    /**
     * Gets all budget-exceeded alert messages from the observer.
     * @return list of alert strings
     */
    public List<String> getAlerts() {
        return alertObserver.getAlerts();
    }

    // ====================== DATA PERSISTENCE ======================

    /**
     * Saves all data (users, transactions, budgets) to CSV files.
     */
    public void saveData() {
        // Ensure data directory exists
        new java.io.File("data").mkdirs();
        fileStorage.saveUsers(users, USERS_FILE);
        if (currentUser != null) {
            fileStorage.saveTransactions(currentUser.getTransactions(), TRANSACTIONS_FILE);
        }
        fileStorage.saveBudgets(financeManager.getBudgets(), BUDGETS_FILE);
    }

    /**
     * Loads all data (users, transactions, budgets) from CSV files.
     */
    public void loadData() {
        // Load users
        users = fileStorage.loadUsers(USERS_FILE);

        // Load budgets
        List<Budget> loadedBudgets = fileStorage.loadBudgets(BUDGETS_FILE);
        for (Budget b : loadedBudgets) {
            financeManager.setBudget(b.getCategory(), b.getLimit());
            Budget managed = financeManager.getBudgetByCategory(b.getCategory());
            if (managed != null) {
                managed.setSpent(b.getSpent());
            }
        }

        // Load transactions and assign to first user if exists
        List<Transaction> loadedTransactions = fileStorage.loadTransactions(TRANSACTIONS_FILE);
        if (!users.isEmpty()) {
            currentUser = users.get(0);
            currentUser.setTransactions(loadedTransactions);
        }
    }

    /**
     * Loads sample data if no saved data exists.
     * Creates a demo user with sample transactions and budgets.
     */
    public void loadSampleData() {
        // Check if data files exist
        java.io.File usersFile = new java.io.File(USERS_FILE);
        if (usersFile.exists()) {
            loadData();
            // Ensure demo user always exists even after loading
            boolean hasDemoUser = false;
            for (User u : users) {
                if ("demo@finance.com".equalsIgnoreCase(u.getEmail())) {
                    hasDemoUser = true;
                    break;
                }
            }
            if (!hasDemoUser) {
                User demoUser = new User("demo001", "Demo User", "demo@finance.com", "demo123");
                users.add(demoUser);
                if (currentUser == null) currentUser = demoUser;
                saveData();
            }
            System.out.println("Loaded data with " + users.size() + " users.");
            return;
        }

        // Create demo user
        User demoUser = new User("demo001", "Demo User", "demo@finance.com", "demo123");
        users.add(demoUser);
        currentUser = demoUser;

        // Add sample transactions
        addIncome("Salary", 2000.0, "2024-03-01", "Salary", "Employer");
        addIncome("Freelance", 500.0, "2024-03-15", "Freelance", "Client");
        addExpense("Rent", 800.0, "2024-03-02", "Housing");
        addExpense("Groceries", 150.0, "2024-03-10", "Food");
        addExpense("Netflix", 15.0, "2024-03-05", "Entertainment");
        addExpense("Gym", 40.0, "2024-03-08", "Health");

        // Set sample budgets (this recalculates spending too)
        setBudget("Food", 200.0);
        setBudget("Entertainment", 50.0);
        setBudget("Housing", 900.0);

        // Save everything
        System.out.println("Sample data loaded successfully!");
    }

    // ====================== JSON HELPERS FOR SERVER ======================

    public String getAlertsJson() {
        List<String> alerts = getAlerts();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"alerts\":[");
        for (int i = 0; i < alerts.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(alerts.get(i)).append("\"");
        }
        sb.append("]}");
        return sb.toString();
    }

    public String getBudgetsJson() {
        List<Budget> budgets = getAllBudgets();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"budgets\":[");
        for (int i = 0; i < budgets.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(budgets.get(i).toJson());
        }
        sb.append("]}");
        return sb.toString();
    }

    public String getTransactionsJson() {
        List<Transaction> transactions = getAllTransactions();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"transactions\":[");
        for (int i = 0; i < transactions.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(transactions.get(i).toJson());
        }
        sb.append("]}");
        return sb.toString();
    }

    public String getMonthlyReportJson(String month) {
        double totalIncome = 0;
        double totalExpense = 0;
        for (Transaction t : getAllTransactions()) {
            if (month != null && !month.isEmpty() && t.getDate() != null && !t.getDate().startsWith(month)) {
                continue;
            }
            if (t instanceof Income) {
                totalIncome += t.getAmount();
            } else if (t instanceof Expense) {
                totalExpense += t.getAmount();
            }
        }
        double netBalance = totalIncome - totalExpense;

        return "{\"totalIncome\":" + totalIncome + ","
                + "\"totalExpense\":" + totalExpense + ","
                + "\"netBalance\":" + netBalance + "}";
    }

    public String getCategoryReportJson() {
        if (getAllTransactions() == null || getAllTransactions().isEmpty()) {
            return "{\"categories\":[]}";
        }
        CategoryReport report = generateCategoryReport();
        java.util.Map<String, Double> breakdown = report.getCategoryBreakdown();

        double totalSpending = 0;
        for (double val : breakdown.values()) {
            totalSpending += val;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"totalSpending\":").append(totalSpending).append(",");
        sb.append("\"categories\":[");
        int count = 0;
        for (java.util.Map.Entry<String, Double> entry : breakdown.entrySet()) {
            if (count > 0) sb.append(",");
            double pct = totalSpending > 0 ? (entry.getValue() / totalSpending) * 100 : 0;
            sb.append("{\"category\":\"").append(entry.getKey()).append("\",")
              .append("\"total\":").append(entry.getValue()).append(",")
              .append("\"percentage\":").append(Math.round(pct * 10.0) / 10.0).append("}");
            count++;
        }
        sb.append("]}");
        return sb.toString();
    }
}

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

public class FinanceService {
    private User currentUser;
    private List<User> users;
    private FinanceManager financeManager;
    private BudgetAlertObserver alertObserver;
    private FileStorage fileStorage;

    private static final String TRANSACTIONS_FILE = "data/transactions.csv";
    private static final String USERS_FILE = "data/users.csv";
    private static final String BUDGETS_FILE = "data/budgets.csv";

    public FinanceService() {
        this.users = new ArrayList<>();
        this.financeManager = new FinanceManager();
        this.alertObserver = new BudgetAlertObserver();
        this.fileStorage = new FileStorage();
        this.financeManager.addObserver(alertObserver);
    }

    public boolean registerUser(String name, String email, String password) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return false;
            }
        }
        String userId = UUID.randomUUID().toString().substring(0, 8);
        User newUser = new User(userId, name, email, password);
        users.add(newUser);
        saveData();
        return true;
    }

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

    public void setCurrentUser(String userId) {
        for (User u : users) {
            if (u.getUserId().equals(userId)) {
                this.currentUser = u;
                return;
            }
        }
    }

    public User getCurrentUser() { return currentUser; }
    public List<User> getUsers() { return users; }

    public Income addIncome(String description, double amount, String date, String category, String source) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Income income = new Income(id, amount, date, description, source, category);
        if (currentUser != null) {
            currentUser.addTransaction(income);
        }
        financeManager.addSpendingToCategory(category, -amount);
        saveData();
        return income;
    }

    public Expense addExpense(String description, double amount, String date, String category) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Expense expense = new Expense(id, amount, date, description, category);
        if (currentUser != null) {
            currentUser.addTransaction(expense);
        }
        financeManager.addSpendingToCategory(category, amount);
        saveData();
        return expense;
    }

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

    public List<Transaction> getAllTransactions() {
        if (currentUser != null) {
            return currentUser.getTransactions();
        }
        return new ArrayList<>();
    }

    public Budget setBudget(String category, double limit) {
        Budget budget = financeManager.setBudget(category, limit);
        recalculateBudgetSpending(budget);
        saveData();
        return budget;
    }

    public List<Budget> getAllBudgets() {
        return financeManager.getBudgets();
    }

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
        if (budget.isExceeded()) {
            financeManager.notifyObservers(budget);
        }
    }

    public MonthlySummaryReport generateMonthlyReport(String month) {
        return new MonthlySummaryReport(month, getAllTransactions());
    }

    public CategoryReport generateCategoryReport() {
        return new CategoryReport(getAllTransactions());
    }

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

    public List<String> getAlerts() {
        return alertObserver.getAlerts();
    }

    public void saveData() {
        new java.io.File("data").mkdirs();
        fileStorage.saveUsers(users, USERS_FILE);
        if (currentUser != null) {
            fileStorage.saveTransactions(currentUser.getTransactions(), TRANSACTIONS_FILE);
        }
        fileStorage.saveBudgets(financeManager.getBudgets(), BUDGETS_FILE);
    }

    public void loadData() {
        users = fileStorage.loadUsers(USERS_FILE);

        List<Budget> loadedBudgets = fileStorage.loadBudgets(BUDGETS_FILE);
        for (Budget b : loadedBudgets) {
            financeManager.setBudget(b.getCategory(), b.getLimit());
            Budget managed = financeManager.getBudgetByCategory(b.getCategory());
            if (managed != null) {
                managed.setSpent(b.getSpent());
            }
        }

        List<Transaction> loadedTransactions = fileStorage.loadTransactions(TRANSACTIONS_FILE);
        if (!users.isEmpty()) {
            currentUser = users.get(0);
            currentUser.setTransactions(loadedTransactions);
        }
    }

    public void initializeData() {
        java.io.File usersFile = new java.io.File(USERS_FILE);
        if (usersFile.exists()) {
            loadData();
            System.out.println("Loaded data with " + users.size() + " users.");
            return;
        }

        System.out.println("No saved data found. Initializing clean slate.");
        User demoUser = new User("demo001", "Demo User", "demo@finance.com", "demo123");
        users.add(demoUser);
        currentUser = demoUser;
        saveData();
    }

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

    public String exportTransactionsToCSV() {
        List<Transaction> transactions = getAllTransactions();
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Type,Description,Category,Amount\n");
        for (Transaction t : transactions) {
            String type = (t instanceof Income) ? "Income" : "Expense";
            String cat = "";
            if (t instanceof com.financetracker.interfaces.Categorizable) {
                cat = ((com.financetracker.interfaces.Categorizable)t).getCategory();
            }
            sb.append(t.getDate()).append(",")
              .append(type).append(",")
              .append("\"").append(t.getDescription().replace("\"", "\"\"")).append("\",")
              .append("\"").append(cat).append("\",")
              .append(t.getAmount()).append("\n");
        }
        return sb.toString();
    }

    public String getSmartInsightsJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"insights\":[");
        
        double income = getTotalIncome();
        double expense = getTotalExpense();
        double balance = income - expense;
        
        List<String> insights = new ArrayList<>();
        
        if (expense > income && income > 0) {
            insights.add("Critical: You have spent more than you earned this period!");
        } else if (balance > 0) {
            insights.add("Great job! You have a positive net balance of $" + balance + ".");
            if (balance > (income * 0.2)) {
                insights.add("You're saving more than 20% of your income. Keep it up!");
            }
        }
        
        List<Budget> budgets = getAllBudgets();
        for (Budget b : budgets) {
            if (b.getSpent() > b.getLimit()) {
                insights.add("Alert: You have exceeded your budget for " + b.getCategory() + " by $" + (b.getSpent() - b.getLimit()) + ".");
            } else if (b.getSpent() > (b.getLimit() * 0.8)) {
                insights.add("Warning: You are nearing your budget limit for " + b.getCategory() + ".");
            }
        }
        
        if (insights.isEmpty()) {
            insights.add("Your finances are looking stable.");
        }
        
        for (int i = 0; i < insights.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(insights.get(i).replace("\"", "\\\"")).append("\"");
        }
        sb.append("]}");
        return sb.toString();
    }
}

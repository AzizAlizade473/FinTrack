package com.financetracker.service;

import com.financetracker.abstract_base.Transaction;
import com.financetracker.model.*;
import com.financetracker.pattern.BudgetAlertObserver;
import com.financetracker.report.MonthlySummaryReport;
import com.financetracker.report.CategoryReport;
import com.financetracker.storage.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FinanceService {
    private List<User> users;
    private FileStorage fileStorage;

    // Optional: we can maintain a global alert observer, or just keep alerts per user. 
    // Since alerts usually apply per session, we will keep a map or generate them dynamically.

    private static final String TRANSACTIONS_FILE = "data/transactions.csv";
    private static final String USERS_FILE = "data/users.csv";
    private static final String BUDGETS_FILE = "data/budgets.csv";

    public FinanceService() {
        this.users = new ArrayList<>();
        this.fileStorage = new FileStorage();
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
                 return "{\"userId\":\"" + u.getUserId() + "\",\"name\":\"" + u.getName() + "\",\"email\":\"" + u.getEmail() + "\"}";
             }
        }
        if ("demo@finance.com".equalsIgnoreCase(email) && "demo123".equals(password)) {
             User dummy = getUser("demo001");
             if (dummy == null) {
                 dummy = new User("demo001", "Demo User", "demo@finance.com", "demo123");
                 users.add(dummy);
                 saveData();
             }
             return "{\"userId\":\"demo001\",\"name\":\"Demo User\",\"email\":\"demo@finance.com\"}";
        }
        return null;
    }

    public User getUser(String userId) {
        for (User u : users) {
            if (u.getUserId().equals(userId)) return u;
        }
        return null;
    }

    public List<User> getUsers() { return users; }

    public Income addIncome(String userId, String description, double amount, String date, String category, String source) {
        User user = getUser(userId);
        if (user == null) return null;

        String id = UUID.randomUUID().toString().substring(0, 8);
        Income income = new Income(id, amount, date, description, source, category);
        user.addTransaction(income);
        
        recalculateBudgetSpending(user, category);
        saveData();
        return income;
    }

    public Expense addExpense(String userId, String description, double amount, String date, String category) {
        User user = getUser(userId);
        if (user == null) return null;

        String id = UUID.randomUUID().toString().substring(0, 8);
        Expense expense = new Expense(id, amount, date, description, category);
        user.addTransaction(expense);
        
        recalculateBudgetSpending(user, category);
        saveData();
        return expense;
    }

    public boolean deleteTransaction(String userId, String transactionId) {
        User user = getUser(userId);
        if (user != null) {
            Transaction toRemove = null;
            for(Transaction t : user.getTransactions()){
                if(t.getId().equals(transactionId)) toRemove = t;
            }
            if(toRemove != null){
                user.removeTransaction(transactionId);
                if(toRemove instanceof com.financetracker.interfaces.Categorizable) {
                    recalculateBudgetSpending(user, ((com.financetracker.interfaces.Categorizable)toRemove).getCategory());
                }
                saveData();
                return true;
            }
        }
        return false;
    }

    public List<Transaction> getAllTransactions(String userId) {
        User user = getUser(userId);
        return user != null ? user.getTransactions() : new ArrayList<>();
    }

    public Budget setBudget(String userId, String category, double limit) {
        User user = getUser(userId);
        if (user == null) return null;

        Budget budget = user.getBudgetByCategory(category);
        if(budget == null){
            budget = new Budget(category, limit);
            user.addBudget(budget);
        } else {
            budget.setLimit(limit);
        }

        recalculateBudgetSpending(user, category);
        saveData();
        return budget;
    }

    public boolean deleteBudget(String userId, String category) {
        User user = getUser(userId);
        if (user != null) {
            boolean removed = user.removeBudget(category);
            if (removed) saveData();
            return removed;
        }
        return false;
    }

    public List<Budget> getAllBudgets(String userId) {
        User user = getUser(userId);
        return user != null ? user.getBudgets() : new ArrayList<>();
    }

    private void recalculateBudgetSpending(User user, String category) {
        if(user == null || category == null) return;
        Budget budget = user.getBudgetByCategory(category);
        if(budget == null) return;

        double totalSpent = 0;
        for (Transaction t : user.getTransactions()) {
            if (t instanceof com.financetracker.interfaces.Categorizable) {
                com.financetracker.interfaces.Categorizable catItem = (com.financetracker.interfaces.Categorizable) t;
                if (catItem.getCategory() != null && catItem.getCategory().equalsIgnoreCase(category)) {
                    if (t instanceof Expense) {
                        totalSpent += t.getAmount();
                    } else if (t instanceof Income) {
                        totalSpent -= t.getAmount();
                    }
                }
            }
        }
        budget.setSpent(totalSpent);
    }

    public MonthlySummaryReport generateMonthlyReport(String userId, String month) {
        return new MonthlySummaryReport(month, getAllTransactions(userId));
    }

    public CategoryReport generateCategoryReport(String userId) {
        return new CategoryReport(getAllTransactions(userId));
    }

    public double getBalance(String userId) {
        return getTotalIncome(userId) - getTotalExpense(userId);
    }

    public double getTotalIncome(String userId) {
        double total = 0;
        for (Transaction t : getAllTransactions(userId)) {
            if (t instanceof Income) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public double getTotalExpense(String userId) {
        double total = 0;
        for (Transaction t : getAllTransactions(userId)) {
            if (t instanceof Expense) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public List<String> getAlerts(String userId) {
        List<String> alerts = new ArrayList<>();
        User user = getUser(userId);
        if (user == null) return alerts;

        BudgetAlertObserver observer = new BudgetAlertObserver();
        for (Budget b : user.getBudgets()) {
            if (b.isExceeded()) {
                observer.onBudgetExceeded(b);
            }
        }
        return observer.getAlerts();
    }

    public void saveData() {
        new java.io.File("data").mkdirs();
        fileStorage.saveUsers(users, USERS_FILE);
        fileStorage.saveTransactions(users, TRANSACTIONS_FILE);
        fileStorage.saveBudgets(users, BUDGETS_FILE);
    }

    public void loadData() {
        users = fileStorage.loadUsers(USERS_FILE);
        fileStorage.loadBudgetsIntoUsers(users, BUDGETS_FILE);
        fileStorage.loadTransactionsIntoUsers(users, TRANSACTIONS_FILE);
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
        saveData();
    }

    public String getAlertsJson(String userId) {
        List<String> alerts = getAlerts(userId);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"alerts\":[");
        for (int i = 0; i < alerts.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(alerts.get(i)).append("\"");
        }
        sb.append("]}");
        return sb.toString();
    }

    public String getBudgetsJson(String userId) {
        List<Budget> budgets = getAllBudgets(userId);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"budgets\":[");
        for (int i = 0; i < budgets.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(budgets.get(i).toJson());
        }
        sb.append("]}");
        return sb.toString();
    }

    public String getTransactionsJson(String userId) {
        List<Transaction> transactions = getAllTransactions(userId);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"transactions\":[");
        for (int i = 0; i < transactions.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(transactions.get(i).toJson());
        }
        sb.append("]}");
        return sb.toString();
    }

    public String getMonthlyReportJson(String userId, String month) {
        double totalIncome = 0;
        double totalExpense = 0;
        for (Transaction t : getAllTransactions(userId)) {
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

    public String getCategoryReportJson(String userId) {
        if (getAllTransactions(userId) == null || getAllTransactions(userId).isEmpty()) {
            return "{\"categories\":[]}";
        }
        CategoryReport report = generateCategoryReport(userId);
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

    public String exportTransactionsToCSV(String userId) {
        List<Transaction> transactions = getAllTransactions(userId);
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

    public String getSmartInsightsJson(String userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"insights\":[");
        
        double income = getTotalIncome(userId);
        double expense = getTotalExpense(userId);
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
        
        List<Budget> budgets = getAllBudgets(userId);
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


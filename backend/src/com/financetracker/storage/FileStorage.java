package com.financetracker.storage;

import com.financetracker.abstract_base.Transaction;
import com.financetracker.model.Income;
import com.financetracker.model.Expense;
import com.financetracker.model.Budget;
import com.financetracker.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file I/O for persisting data to CSV files.
 * Uses BufferedReader and BufferedWriter for all file operations.
 */
public class FileStorage {

    /**
     * Saves a list of transactions to a CSV file.
     * Format: type,id,amount,date,description,category,source
     * @param transactions the transactions to save
     * @param filename the CSV file path
     */
    public void saveTransactions(List<Transaction> transactions, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write header
            writer.write("type,id,amount,date,description,category,source");
            writer.newLine();
            // Write each transaction
            for (Transaction t : transactions) {
                String type = t.getType();
                String category = "";
                String source = "";
                if (t instanceof Income) {
                    Income inc = (Income) t;
                    category = inc.getCategory();
                    source = inc.getSource();
                } else if (t instanceof Expense) {
                    Expense exp = (Expense) t;
                    category = exp.getCategory();
                }
                writer.write(type + "," + t.getId() + "," + t.getAmount() + ","
                        + t.getDate() + "," + escapeCSV(t.getDescription()) + ","
                        + escapeCSV(category) + "," + escapeCSV(source));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving transactions: " + e.getMessage());
        }
    }

    /**
     * Loads transactions from a CSV file.
     * @param filename the CSV file path
     * @return list of loaded transactions
     */
    public List<Transaction> loadTransactions(String filename) {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) {
            return transactions; // Return empty list if file doesn't exist
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    String type = parts[0].trim();
                    String id = parts[1].trim();
                    double amount = Double.parseDouble(parts[2].trim());
                    String date = parts[3].trim();
                    String description = unescapeCSV(parts[4].trim());
                    String category = unescapeCSV(parts[5].trim());
                    String source = parts.length > 6 ? unescapeCSV(parts[6].trim()) : "";

                    if ("INCOME".equals(type)) {
                        transactions.add(new Income(id, amount, date, description, source, category));
                    } else if ("EXPENSE".equals(type)) {
                        transactions.add(new Expense(id, amount, date, description, category));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
        }
        return transactions;
    }

    /**
     * Saves a list of users to a CSV file.
     * Format: userId,name,email,password
     * @param users the users to save
     * @param filename the CSV file path
     */
    public void saveUsers(List<User> users, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write header
            writer.write("userId,name,email,password");
            writer.newLine();
            // Write each user
            for (User user : users) {
                writer.write(user.getUserId() + "," + escapeCSV(user.getName()) + ","
                        + escapeCSV(user.getEmail()) + "," + escapeCSV(user.getPassword()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    /**
     * Loads users from a CSV file.
     * @param filename the CSV file path
     * @return list of loaded users
     */
    public List<User> loadUsers(String filename) {
        List<User> users = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) {
            return users; // Return empty list if file doesn't exist
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    String userId = parts[0].trim();
                    String name = unescapeCSV(parts[1].trim());
                    String email = unescapeCSV(parts[2].trim());
                    String password = unescapeCSV(parts[3].trim());
                    users.add(new User(userId, name, email, password));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    /**
     * Saves budgets to a CSV file.
     * Format: category,limit,spent
     * @param budgets the budgets to save
     * @param filename the CSV file path
     */
    public void saveBudgets(List<Budget> budgets, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("category,limit,spent");
            writer.newLine();
            for (Budget b : budgets) {
                writer.write(escapeCSV(b.getCategory()) + "," + b.getLimit() + "," + b.getSpent());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving budgets: " + e.getMessage());
        }
    }

    /**
     * Loads budgets from a CSV file.
     * @param filename the CSV file path
     * @return list of loaded budgets
     */
    public List<Budget> loadBudgets(String filename) {
        List<Budget> budgets = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) {
            return budgets;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    String category = unescapeCSV(parts[0].trim());
                    double limit = Double.parseDouble(parts[1].trim());
                    double spent = Double.parseDouble(parts[2].trim());
                    Budget b = new Budget(category, limit);
                    b.setSpent(spent);
                    budgets.add(b);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading budgets: " + e.getMessage());
        }
        return budgets;
    }

    /**
     * Escapes a CSV field value (replaces commas with semicolons).
     * @param value the value to escape
     * @return escaped value
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace(",", ";");
    }

    /**
     * Unescapes a CSV field value.
     * @param value the value to unescape
     * @return unescaped value
     */
    private String unescapeCSV(String value) {
        if (value == null) return "";
        return value.replace(";", ",");
    }
}

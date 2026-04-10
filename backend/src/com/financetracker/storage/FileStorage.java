package com.financetracker.storage;

import com.financetracker.abstract_base.Transaction;
import com.financetracker.model.Income;
import com.financetracker.model.Expense;
import com.financetracker.model.Budget;
import com.financetracker.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {

    public void saveTransactions(List<User> users, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("userId,type,id,amount,date,description,category,source");
            writer.newLine();
            for (User user : users) {
                for (Transaction t : user.getTransactions()) {
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
                    writer.write(user.getUserId() + "," + type + "," + t.getId() + "," + t.getAmount() + ","
                            + t.getDate() + "," + escapeCSV(t.getDescription()) + ","
                            + escapeCSV(category) + "," + escapeCSV(source));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving transactions: " + e.getMessage());
        }
    }

    public void loadTransactionsIntoUsers(List<User> users, String filename) {
        File file = new File(filename);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 7) {
                    String userId = parts[0].trim();
                    String type = parts[1].trim();
                    String id = parts[2].trim();
                    double amount = Double.parseDouble(parts[3].trim());
                    String date = parts[4].trim();
                    String description = unescapeCSV(parts[5].trim());
                    String category = unescapeCSV(parts[6].trim());
                    String source = parts.length > 7 ? unescapeCSV(parts[7].trim()) : "";

                    User user = findUser(users, userId);
                    if (user != null) {
                        if ("INCOME".equals(type)) {
                            user.getTransactions().add(new Income(id, amount, date, description, source, category));
                        } else if ("EXPENSE".equals(type)) {
                            user.getTransactions().add(new Expense(id, amount, date, description, category));
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
        }
    }

    public void saveUsers(List<User> users, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("userId,name,email,password");
            writer.newLine();
            for (User user : users) {
                writer.write(user.getUserId() + "," + escapeCSV(user.getName()) + ","
                        + escapeCSV(user.getEmail()) + "," + escapeCSV(user.getPassword()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public List<User> loadUsers(String filename) {
        List<User> users = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return users;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
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

    public void saveBudgets(List<User> users, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("userId,category,limit,spent");
            writer.newLine();
            for (User user : users) {
                for (Budget b : user.getBudgets()) {
                    writer.write(user.getUserId() + "," + escapeCSV(b.getCategory()) + "," + b.getLimit() + "," + b.getSpent());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving budgets: " + e.getMessage());
        }
    }

    public void loadBudgetsIntoUsers(List<User> users, String filename) {
        File file = new File(filename);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    String userId = parts[0].trim();
                    String category = unescapeCSV(parts[1].trim());
                    double limit = Double.parseDouble(parts[2].trim());
                    double spent = Double.parseDouble(parts[3].trim());
                    
                    User user = findUser(users, userId);
                    if (user != null) {
                        Budget b = new Budget(category, limit);
                        b.setSpent(spent);
                        user.addBudget(b);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading budgets: " + e.getMessage());
        }
    }

    private User findUser(List<User> users, String userId) {
        for (User u : users) {
             if (u.getUserId().equals(userId)) return u;
        }
        return null;
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace(",", ";");
    }

    private String unescapeCSV(String value) {
        if (value == null) return "";
        return value.replace(";", ",");
    }
}


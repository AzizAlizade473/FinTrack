package com.financetracker.model;

import com.financetracker.abstract_base.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private String userId;
    private String name;
    private String email;
    private String password;
    private List<Transaction> transactions;
    private List<Budget> budgets;

    public User(String userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.transactions = new ArrayList<>();
        this.budgets = new ArrayList<>();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    public boolean removeTransaction(String transactionId) {
        return this.transactions.removeIf(t -> t.getId().equals(transactionId));
    }

    public List<Budget> getBudgets() { return budgets; }
    public void setBudgets(List<Budget> budgets) { this.budgets = budgets; }

    public void addBudget(Budget budget) {
        this.budgets.add(budget);
    }

    public Budget getBudgetByCategory(String category) {
        for (Budget b : budgets) {
            if (b.getCategory().equalsIgnoreCase(category)) {
                return b;
            }
        }
        return null;
    }

    public boolean removeBudget(String category) {
        return this.budgets.removeIf(b -> b.getCategory().equalsIgnoreCase(category));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User [id=" + userId + ", name=" + name + ", email=" + email + ", transactions=" + transactions.size() + ", budgets=" + budgets.size() + "]";
    }
}


package com.financetracker.pattern;

import com.financetracker.model.Budget;
import java.util.ArrayList;
import java.util.List;

/**
 * Subject class in the Observer design pattern.
 * Manages a list of observers and notifies them when a budget is exceeded.
 * Also maintains the list of budgets.
 */
public class FinanceManager {
    /** List of registered observers */
    private List<FinanceObserver> observers;
    /** List of budgets being tracked */
    private List<Budget> budgets;

    /**
     * Constructs a new FinanceManager with empty observer and budget lists.
     */
    public FinanceManager() {
        this.observers = new ArrayList<>();
        this.budgets = new ArrayList<>();
    }

    /**
     * Registers an observer to receive budget-exceeded notifications.
     * @param observer the observer to add
     */
    public void addObserver(FinanceObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer from the notification list.
     * @param observer the observer to remove
     */
    public void removeObserver(FinanceObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all registered observers that a budget has been exceeded.
     * @param budget the exceeded budget
     */
    public void notifyObservers(Budget budget) {
        for (FinanceObserver observer : observers) {
            observer.onBudgetExceeded(budget);
        }
    }

    /**
     * Gets the list of all budgets.
     * @return list of budgets
     */
    public List<Budget> getBudgets() {
        return budgets;
    }

    /**
     * Sets or updates a budget for a category.
     * If a budget already exists for that category, updates the limit.
     * @param category the budget category
     * @param limit the spending limit
     * @return the created or updated budget
     */
    public Budget setBudget(String category, double limit) {
        // Check if budget already exists for this category
        for (Budget b : budgets) {
            if (b.getCategory().equalsIgnoreCase(category)) {
                b.setLimit(limit);
                return b;
            }
        }
        // Create new budget
        Budget newBudget = new Budget(category, limit);
        budgets.add(newBudget);
        return newBudget;
    }

    /**
     * Finds a budget by category name.
     * @param category the category to search for
     * @return the matching budget or null if not found
     */
    public Budget getBudgetByCategory(String category) {
        for (Budget b : budgets) {
            if (b.getCategory().equalsIgnoreCase(category)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Adds spending to a category's budget and notifies observers if exceeded.
     * @param category the spending category
     * @param amount the amount spent
     */
    public void addSpendingToCategory(String category, double amount) {
        Budget budget = getBudgetByCategory(category);
        if (budget != null) {
            budget.addSpending(amount);
            // Notify observers if the budget is now exceeded
            if (budget.isExceeded()) {
                notifyObservers(budget);
            }
        }
    }
}

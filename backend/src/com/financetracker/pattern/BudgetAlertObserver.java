package com.financetracker.pattern;

import com.financetracker.model.Budget;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete observer that stores budget-exceeded alert messages.
 * Implements FinanceObserver as part of the Observer design pattern.
 */
public class BudgetAlertObserver implements FinanceObserver {
    /** List of alert messages generated when budgets are exceeded */
    private List<String> alerts;

    /**
     * Constructs a new BudgetAlertObserver with empty alert list.
     */
    public BudgetAlertObserver() {
        this.alerts = new ArrayList<>();
    }

    /**
     * Called when a budget is exceeded. Adds an alert message.
     * @param budget the exceeded budget
     */
    @Override
    public void onBudgetExceeded(Budget budget) {
        // Build a descriptive alert message
        String alert = "⚠️ Budget exceeded for category '" + budget.getCategory()
                + "'! Limit: $" + budget.getLimit()
                + ", Spent: $" + budget.getSpent()
                + ", Over by: $" + String.format("%.2f", budget.getSpent() - budget.getLimit());
        alerts.add(alert);
    }

    /**
     * Gets all alert messages.
     * @return list of alert strings
     */
    public List<String> getAlerts() {
        return alerts;
    }

    /**
     * Clears all stored alerts.
     */
    public void clearAlerts() {
        alerts.clear();
    }
}

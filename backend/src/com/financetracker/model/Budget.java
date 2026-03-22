package com.financetracker.model;

import com.financetracker.interfaces.Trackable;

/**
 * Represents a budget for a specific spending category.
 * Tracks spending against a defined limit.
 */
public class Budget implements Trackable {
    /** The category this budget applies to */
    private String category;
    /** The maximum spending limit */
    private double limit;
    /** The current amount spent */
    private double spent;

    /**
     * Constructs a new Budget.
     * @param category the budget category
     * @param limit the spending limit
     */
    public Budget(String category, double limit) {
        this.category = category;
        this.limit = limit;
        this.spent = 0.0;
    }

    /**
     * Adds spending to this budget.
     * @param amount the amount to add
     */
    public void addSpending(double amount) {
        this.spent += amount;
    }

    /**
     * Checks if spending has exceeded the budget limit.
     * @return true if spent > limit
     */
    public boolean isExceeded() {
        return spent > limit;
    }

    /**
     * Gets the remaining budget amount.
     * @return limit minus spent (can be negative if exceeded)
     */
    public double getRemainingBudget() {
        return limit - spent;
    }

    // --- Getters and Setters ---

    /** @return the budget category */
    public String getCategory() { return category; }

    /** @param category the category to set */
    public void setCategory(String category) { this.category = category; }

    /** @return the spending limit */
    public double getLimit() { return limit; }

    /** @param limit the limit to set */
    public void setLimit(double limit) { this.limit = limit; }

    /** @return the current spent amount */
    public double getSpent() { return spent; }

    /** @param spent the spent amount to set */
    public void setSpent(double spent) { this.spent = spent; }

    /**
     * Tracks this budget.
     * @return tracking information string
     */
    @Override
    public String track() {
        return "Tracking Budget: " + category + " - Spent: $" + spent + " / Limit: $" + limit;
    }

    /**
     * Returns a summary of this budget.
     * @return summary string
     */
    @Override
    public String getSummary() {
        String status = isExceeded() ? "EXCEEDED" : "OK";
        return "Budget [" + category + "] $" + spent + " / $" + limit + " (" + status + ")";
    }
}

package com.financetracker.model;

import com.financetracker.abstract_base.Transaction;
import com.financetracker.interfaces.Trackable;
import com.financetracker.interfaces.Categorizable;

/**
 * Represents an expense transaction.
 * Extends Transaction and implements Trackable and Categorizable.
 */
public class Expense extends Transaction implements Trackable, Categorizable {
    /** The category of this expense (e.g., Food, Housing) */
    private String category;

    /**
     * Constructs a new Expense transaction.
     * @param id unique identifier
     * @param amount expense amount
     * @param date date string (YYYY-MM-DD)
     * @param description description of the expense
     * @param category expense category
     */
    public Expense(String id, double amount, String date, String description, String category) {
        super(id, amount, date, description);
        this.category = category;
    }

    /** @return "EXPENSE" */
    @Override
    public String getType() {
        return "EXPENSE";
    }

    /** @return the expense category */
    @Override
    public String getCategory() { return category; }

    /** @param category the category to set */
    @Override
    public void setCategory(String category) { this.category = category; }

    /**
     * Tracks this expense transaction.
     * @return tracking information string
     */
    @Override
    public String track() {
        return "Tracking Expense: " + getDescription() + " - $" + getAmount();
    }

    @Override
    public String toJson() {
        return "{\"id\":\"" + getId() + "\"," +
               "\"type\":\"" + getType() + "\"," +
               "\"amount\":" + getAmount() + "," +
               "\"date\":\"" + getDate() + "\"," +
               "\"description\":\"" + getDescription() + "\"," +
               "\"category\":\"" + getCategory() + "\"}";
    }

    /**
     * Returns a summary of this expense.
     * @return summary string
     */
    @Override
    public String getSummary() {
        return "Expense: " + getDescription() + " | Amount: $" + getAmount() + " | Category: " + category;
    }
}

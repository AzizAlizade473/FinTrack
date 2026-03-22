package com.financetracker.model;

import com.financetracker.abstract_base.Transaction;
import com.financetracker.interfaces.Trackable;
import com.financetracker.interfaces.Categorizable;

/**
 * Represents an income transaction.
 * Extends Transaction and implements Trackable and Categorizable.
 */
public class Income extends Transaction implements Trackable, Categorizable {
    /** The source of income (e.g., Employer, Client) */
    private String source;
    /** The category of this income (e.g., Salary, Freelance) */
    private String category;

    /**
     * Constructs a new Income transaction.
     * @param id unique identifier
     * @param amount income amount
     * @param date date string (YYYY-MM-DD)
     * @param description description of the income
     * @param source source of income
     * @param category income category
     */
    public Income(String id, double amount, String date, String description, String source, String category) {
        super(id, amount, date, description);
        this.source = source;
        this.category = category;
    }

    /** @return "INCOME" */
    @Override
    public String getType() {
        return "INCOME";
    }

    /** @return the income source */
    public String getSource() { return source; }

    /** @param source the source to set */
    public void setSource(String source) { this.source = source; }

    /** @return the income category */
    @Override
    public String getCategory() { return category; }

    /** @param category the category to set */
    @Override
    public void setCategory(String category) { this.category = category; }

    /**
     * Tracks this income transaction.
     * @return tracking information string
     */
    @Override
    public String track() {
        return "Tracking Income: " + getDescription() + " - $" + getAmount();
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
     * Returns a summary of this income.
     * @return summary string
     */
    @Override
    public String getSummary() {
        return "Income: " + getDescription() + " | Amount: $" + getAmount() + " | Category: " + category + " | Source: " + source;
    }
}

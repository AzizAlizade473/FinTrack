package com.financetracker.model;

import com.financetracker.abstract_base.Transaction;
import com.financetracker.interfaces.Trackable;
import com.financetracker.interfaces.Categorizable;

public class Expense extends Transaction implements Trackable, Categorizable {
    private String category;

    public Expense(String id, double amount, String date, String description, String category) {
        super(id, amount, date, description);
        this.category = category;
    }

    @Override
    public String getType() { return "EXPENSE"; }

    @Override
    public String getCategory() { return category; }

    @Override
    public void setCategory(String category) { this.category = category; }

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

    @Override
    public String getSummary() {
        return "Expense: " + getDescription() + " | Amount: $" + getAmount() + " | Category: " + category;
    }
}

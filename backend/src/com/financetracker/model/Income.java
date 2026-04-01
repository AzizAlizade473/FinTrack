package com.financetracker.model;

import com.financetracker.abstract_base.Transaction;
import com.financetracker.interfaces.Trackable;
import com.financetracker.interfaces.Categorizable;

public class Income extends Transaction implements Trackable, Categorizable {
    private String source;
    private String category;

    public Income(String id, double amount, String date, String description, String source, String category) {
        super(id, amount, date, description);
        this.source = source;
        this.category = category;
    }

    @Override
    public String getType() { return "INCOME"; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    @Override
    public String getCategory() { return category; }

    @Override
    public void setCategory(String category) { this.category = category; }

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

    @Override
    public String getSummary() {
        return "Income: " + getDescription() + " | Amount: $" + getAmount() + " | Category: " + category + " | Source: " + source;
    }
}

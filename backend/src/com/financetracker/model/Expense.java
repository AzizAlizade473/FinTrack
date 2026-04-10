package com.financetracker.model;

import com.financetracker.abstract_base.Transaction;
import com.financetracker.interfaces.Trackable;
import com.financetracker.interfaces.Categorizable;

public class Expense extends Transaction implements Trackable, Categorizable {
    private String category;
    private String merchant;

    public Expense(String id, double amount, String date, String description, String category, String merchant) {
        super(id, amount, date, description);
        this.category = category;
        this.merchant = merchant != null ? merchant : "";
    }

    public Expense(String id, double amount, String date, String description, String category) {
        this(id, amount, date, description, category, "");
    }

    @Override
    public String getType() { return "EXPENSE"; }

    @Override
    public String getCategory() { return category; }

    @Override
    public void setCategory(String category) { this.category = category; }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    @Override
    public String track() {
        return "Tracking Expense: " + getDescription() + " at " + merchant + " - $" + getAmount();
    }

    @Override
    public String toJson() {
        return "{\"id\":\"" + getId() + "\"," +
               "\"type\":\"" + getType() + "\"," +
               "\"amount\":" + getAmount() + "," +
               "\"date\":\"" + getDate() + "\"," +
               "\"description\":\"" + getDescription() + "\"," +
               "\"category\":\"" + getCategory() + "\"," +
               "\"merchant\":\"" + merchant.replace("\"", "\\\"") + "\"}";
    }

    @Override
    public String getSummary() {
        return "Expense: " + getDescription() + " | Merchant: " + merchant + " | Amount: $" + getAmount() + " | Category: " + category;
    }
}

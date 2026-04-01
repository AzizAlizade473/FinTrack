package com.financetracker.model;

public class Budget implements com.financetracker.interfaces.Trackable {
    private String category;
    private double limit;
    private double spent;

    public Budget(String category, double limit) {
        this.category = category;
        this.limit = limit;
        this.spent = 0.0;
    }

    public void addSpending(double amount) { this.spent += amount; }
    public boolean isExceeded() { return spent > limit; }
    public double getRemainingBudget() { return limit - spent; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getLimit() { return limit; }
    public void setLimit(double limit) { this.limit = limit; }
    public double getSpent() { return spent; }
    public void setSpent(double spent) { this.spent = spent; }

    @Override
    public String track() {
        return "Tracking Budget: " + category + " - Spent: $" + spent + " / Limit: $" + limit;
    }

    public String toJson() {
        return "{\"category\":\"" + category + "\",\"limit\":" + limit + ",\"spent\":" + spent + "}";
    }

    @Override
    public String getSummary() {
        String status = isExceeded() ? "EXCEEDED" : "OK";
        return "Budget [" + category + "] $" + spent + " / $" + limit + " (" + status + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Budget)) return false;
        Budget budget = (Budget) o;
        return java.util.Objects.equals(category, budget.category);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(category);
    }
}

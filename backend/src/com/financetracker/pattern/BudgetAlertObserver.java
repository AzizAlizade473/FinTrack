package com.financetracker.pattern;

import com.financetracker.model.Budget;
import java.util.ArrayList;
import java.util.List;

public class BudgetAlertObserver implements FinanceObserver {
    private List<String> alerts;

    public BudgetAlertObserver() {
        this.alerts = new ArrayList<>();
    }

    @Override
    public void onBudgetExceeded(Budget budget) {
        String alert = "⚠️ Budget exceeded for category '" + budget.getCategory()
                + "'! Limit: $" + budget.getLimit()
                + ", Spent: $" + budget.getSpent()
                + ", Over by: $" + String.format("%.2f", budget.getSpent() - budget.getLimit());
        alerts.add(alert);
    }

    public List<String> getAlerts() { return alerts; }

    public void clearAlerts() { alerts.clear(); }
}

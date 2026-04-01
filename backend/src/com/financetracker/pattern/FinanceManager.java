package com.financetracker.pattern;

import com.financetracker.model.Budget;
import java.util.ArrayList;
import java.util.List;

public class FinanceManager {
    private List<FinanceObserver> observers;
    private List<Budget> budgets;

    public FinanceManager() {
        this.observers = new ArrayList<>();
        this.budgets = new ArrayList<>();
    }

    public void addObserver(FinanceObserver observer) { observers.add(observer); }
    public void removeObserver(FinanceObserver observer) { observers.remove(observer); }

    public void notifyObservers(Budget budget) {
        for (FinanceObserver observer : observers) {
            observer.onBudgetExceeded(budget);
        }
    }

    public List<Budget> getBudgets() { return budgets; }

    public Budget setBudget(String category, double limit) {
        for (Budget b : budgets) {
            if (b.getCategory().equalsIgnoreCase(category)) {
                b.setLimit(limit);
                return b;
            }
        }
        Budget newBudget = new Budget(category, limit);
        budgets.add(newBudget);
        return newBudget;
    }

    public Budget getBudgetByCategory(String category) {
        for (Budget b : budgets) {
            if (b.getCategory().equalsIgnoreCase(category)) {
                return b;
            }
        }
        return null;
    }

    public void addSpendingToCategory(String category, double amount) {
        Budget budget = getBudgetByCategory(category);
        if (budget != null) {
            budget.addSpending(amount);
            if (budget.isExceeded()) {
                notifyObservers(budget);
            }
        }
    }
}

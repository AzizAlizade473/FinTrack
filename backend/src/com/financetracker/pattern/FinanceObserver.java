package com.financetracker.pattern;

import com.financetracker.model.Budget;

public interface FinanceObserver {
    void onBudgetExceeded(Budget budget);
}

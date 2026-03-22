package com.financetracker.pattern;

import com.financetracker.model.Budget;

/**
 * Observer interface for the Observer design pattern.
 * Observers are notified when a budget is exceeded.
 */
public interface FinanceObserver {
    /**
     * Called when a budget has been exceeded.
     * @param budget the budget that was exceeded
     */
    void onBudgetExceeded(Budget budget);
}

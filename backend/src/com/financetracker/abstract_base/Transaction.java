package com.financetracker.abstract_base;

import java.util.Objects;

/**
 * Abstract base class for all financial transactions.
 * Provides common fields and behavior for Income and Expense.
 */
public abstract class Transaction {
    /** Unique identifier for this transaction */
    private String id;
    /** Monetary amount of the transaction */
    private double amount;
    /** Date of the transaction in YYYY-MM-DD format */
    private String date;
    /** Description of the transaction */
    private String description;

    /**
     * Constructs a new Transaction.
     * @param id unique identifier
     * @param amount monetary amount
     * @param date date string (YYYY-MM-DD)
     * @param description description of the transaction
     */
    public Transaction(String id, double amount, String date, String description) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    /**
     * Returns the type of this transaction.
     * @return "INCOME" or "EXPENSE"
     */
    public abstract String getType();

    // --- Getters and Setters ---

    /** @return the transaction id */
    public String getId() { return id; }

    /** @param id the id to set */
    public void setId(String id) { this.id = id; }

    /** @return the transaction amount */
    public double getAmount() { return amount; }

    /** @param amount the amount to set */
    public void setAmount(double amount) { this.amount = amount; }

    /** @return the transaction date */
    public String getDate() { return date; }

    /** @param date the date to set */
    public void setDate(String date) { this.date = date; }

    /** @return the transaction description */
    public String getDescription() { return description; }

    /** @param description the description to set */
    public void setDescription(String description) { this.description = description; }

    /**
     * Returns a string representation of this transaction.
     * @return formatted string with id, type, amount, date, and description
     */
    @Override
    public String toString() {
        return getType() + " [id=" + id + ", amount=" + amount + ", date=" + date + ", desc=" + description + "]";
    }

    /**
     * Checks equality based on transaction id.
     * @param o the object to compare
     * @return true if both transactions have the same id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Returns hash code based on transaction id.
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

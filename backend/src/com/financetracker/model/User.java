package com.financetracker.model;

import com.financetracker.abstract_base.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a user of the finance tracker.
 * Demonstrates Composition: User HAS-A List of Transactions.
 * When a User is removed, their transactions go with them.
 */
public class User {
    /** Unique user identifier */
    private String userId;
    /** User's display name */
    private String name;
    /** User's email address */
    private String email;
    /** User's password */
    private String password;
    /** Composition: user owns these transactions */
    private List<Transaction> transactions;

    /**
     * Constructs a new User with an empty transaction list.
     * @param userId unique identifier
     * @param name display name
     * @param email email address
     * @param password password
     */
    public User(String userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.transactions = new ArrayList<>(); // Composition — transactions belong to this user
    }

    // --- Getters and Setters ---

    /** @return the user id */
    public String getUserId() { return userId; }

    /** @param userId the id to set */
    public void setUserId(String userId) { this.userId = userId; }

    /** @return the user name */
    public String getName() { return name; }

    /** @param name the name to set */
    public void setName(String name) { this.name = name; }

    /** @return the user email */
    public String getEmail() { return email; }

    /** @param email the email to set */
    public void setEmail(String email) { this.email = email; }

    /** @return the user password */
    public String getPassword() { return password; }

    /** @param password the password to set */
    public void setPassword(String password) { this.password = password; }

    /** @return the list of transactions (composition) */
    public List<Transaction> getTransactions() { return transactions; }

    /** @param transactions the transactions list to set */
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    /**
     * Adds a transaction to this user's list.
     * @param transaction the transaction to add
     */
    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    /**
     * Removes a transaction from this user's list by id.
     * @param transactionId the id of the transaction to remove
     * @return true if a transaction was removed
     */
    public boolean removeTransaction(String transactionId) {
        return this.transactions.removeIf(t -> t.getId().equals(transactionId));
    }

    /**
     * Checks equality based on userId.
     * @param o the object to compare
     * @return true if both users have the same userId
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    /**
     * Returns hash code based on userId.
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    /**
     * Returns a string representation of this user.
     * @return formatted string
     */
    @Override
    public String toString() {
        return "User [id=" + userId + ", name=" + name + ", email=" + email + ", transactions=" + transactions.size() + "]";
    }
}

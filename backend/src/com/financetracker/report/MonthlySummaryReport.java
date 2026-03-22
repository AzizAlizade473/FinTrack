package com.financetracker.report;

import com.financetracker.abstract_base.Report;
import com.financetracker.abstract_base.Transaction;
import com.financetracker.interfaces.Exportable;
import com.financetracker.model.Income;
import com.financetracker.model.Expense;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Generates a monthly summary report showing total income, expenses, and net balance.
 * Extends Report and implements Exportable for file output.
 */
public class MonthlySummaryReport extends Report implements Exportable {
    /** List of transactions to generate the report from */
    private List<Transaction> transactions;
    /** The month to filter by (e.g., "2024-03") */
    private String month;

    /**
     * Constructs a MonthlySummaryReport for the given month.
     * @param month the month to report on (YYYY-MM format)
     * @param transactions the list of transactions to analyze
     */
    public MonthlySummaryReport(String month, List<Transaction> transactions) {
        super("Monthly Summary Report - " + month, java.time.LocalDate.now().toString());
        this.month = month;
        this.transactions = transactions;
    }

    /**
     * Generates the monthly summary report content.
     * Calculates total income, total expenses, and net balance for the month.
     * @return formatted report string
     */
    @Override
    public String generate() {
        double totalIncome = 0;
        double totalExpense = 0;

        // Filter transactions by month and calculate totals
        for (Transaction t : transactions) {
            if (t.getDate().startsWith(month)) {
                if (t instanceof Income) {
                    totalIncome += t.getAmount();
                } else if (t instanceof Expense) {
                    totalExpense += t.getAmount();
                }
            }
        }

        double netBalance = totalIncome - totalExpense;

        // Build formatted report string
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("  ").append(getTitle()).append("\n");
        sb.append("  Generated: ").append(getGeneratedDate()).append("\n");
        sb.append("========================================\n");
        sb.append(String.format("  Total Income:   $%.2f%n", totalIncome));
        sb.append(String.format("  Total Expenses: $%.2f%n", totalExpense));
        sb.append(String.format("  Net Balance:    $%.2f%n", netBalance));
        sb.append("========================================\n");

        return sb.toString();
    }

    /**
     * Returns total income for the month.
     * @return total income
     */
    public double getTotalIncome() {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.getDate().startsWith(month) && t instanceof Income) {
                total += t.getAmount();
            }
        }
        return total;
    }

    /**
     * Returns total expenses for the month.
     * @return total expenses
     */
    public double getTotalExpense() {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.getDate().startsWith(month) && t instanceof Expense) {
                total += t.getAmount();
            }
        }
        return total;
    }

    /**
     * Exports the report to a text file using BufferedWriter.
     * @param filename the file to write the report to
     */
    @Override
    public void exportToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(generate());
            System.out.println("Monthly report exported to: " + filename);
        } catch (IOException e) {
            System.err.println("Error exporting monthly report: " + e.getMessage());
        }
    }
}

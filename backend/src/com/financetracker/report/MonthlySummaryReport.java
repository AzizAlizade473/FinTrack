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

public class MonthlySummaryReport extends Report implements Exportable {
    private List<Transaction> transactions;
    private String month;

    public MonthlySummaryReport(String month, List<Transaction> transactions) {
        super("Monthly Summary Report - " + month, java.time.LocalDate.now().toString());
        this.month = month;
        this.transactions = transactions;
    }

    @Override
    public String generate() {
        double totalIncome = 0;
        double totalExpense = 0;

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

    public double getTotalIncome() {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.getDate().startsWith(month) && t instanceof Income) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public double getTotalExpense() {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.getDate().startsWith(month) && t instanceof Expense) {
                total += t.getAmount();
            }
        }
        return total;
    }

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

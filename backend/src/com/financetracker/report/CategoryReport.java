package com.financetracker.report;

import com.financetracker.abstract_base.Report;
import com.financetracker.abstract_base.Transaction;
import com.financetracker.interfaces.Exportable;
import com.financetracker.model.Expense;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CategoryReport extends Report implements Exportable {
    private List<Transaction> transactions;

    public CategoryReport(List<Transaction> transactions) {
        super("Category Breakdown Report", java.time.LocalDate.now().toString());
        this.transactions = transactions;
    }

    @Override
    public String generate() {
        Map<String, Double> categoryMap = getCategoryBreakdown();

        double totalSpending = 0;
        for (double val : categoryMap.values()) {
            totalSpending += val;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("  ").append(getTitle()).append("\n");
        sb.append("  Generated: ").append(getGeneratedDate()).append("\n");
        sb.append("========================================\n");
        sb.append(String.format("  %-20s %-12s %-8s%n", "Category", "Spent", "% of Total"));
        sb.append("  ----------------------------------------\n");

        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            double percentage = totalSpending > 0 ? (entry.getValue() / totalSpending) * 100 : 0;
            sb.append(String.format("  %-20s $%-11.2f %.1f%%%n", entry.getKey(), entry.getValue(), percentage));
        }

        sb.append("  ----------------------------------------\n");
        sb.append(String.format("  %-20s $%-11.2f 100.0%%%n", "TOTAL", totalSpending));
        sb.append("========================================\n");

        return sb.toString();
    }

    public Map<String, Double> getCategoryBreakdown() {
        Map<String, Double> categoryMap = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if (t instanceof Expense) {
                Expense exp = (Expense) t;
                String cat = exp.getCategory();
                categoryMap.put(cat, categoryMap.getOrDefault(cat, 0.0) + exp.getAmount());
            }
        }
        return categoryMap;
    }

    @Override
    public void exportToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(generate());
            System.out.println("Category report exported to: " + filename);
        } catch (IOException e) {
            System.err.println("Error exporting category report: " + e.getMessage());
        }
    }
}

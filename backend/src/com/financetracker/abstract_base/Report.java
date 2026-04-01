package com.financetracker.abstract_base;

public abstract class Report {
    private String title;
    private String generatedDate;

    public Report(String title, String generatedDate) {
        this.title = title;
        this.generatedDate = generatedDate;
    }

    public abstract String generate();

    public String getTitle() { return title; }
    public String getGeneratedDate() { return generatedDate; }
}

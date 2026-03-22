package com.financetracker.abstract_base;

/**
 * Abstract base class for all financial reports.
 * Subclasses must implement the generate() method to produce report content.
 */
public abstract class Report {
    /** Title of the report */
    private String title;
    /** Date the report was generated */
    private String generatedDate;

    /**
     * Constructs a new Report.
     * @param title the report title
     * @param generatedDate the date the report is generated
     */
    public Report(String title, String generatedDate) {
        this.title = title;
        this.generatedDate = generatedDate;
    }

    /**
     * Generates the report content.
     * @return formatted report string
     */
    public abstract String generate();

    /**
     * Gets the report title.
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the generated date.
     * @return the generated date string
     */
    public String getGeneratedDate() {
        return generatedDate;
    }
}

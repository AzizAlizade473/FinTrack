package com.financetracker.interfaces;

/**
 * Interface for objects that can be exported to a file.
 */
public interface Exportable {
    /**
     * Exports the object's data to a file.
     * @param filename the name of the file to export to
     */
    void exportToFile(String filename);
}

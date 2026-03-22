package com.financetracker.interfaces;

/**
 * Interface for objects that can be assigned a category.
 */
public interface Categorizable {
    /**
     * Gets the category of this object.
     * @return the category string
     */
    String getCategory();

    /**
     * Sets the category of this object.
     * @param category the category to assign
     */
    void setCategory(String category);
}

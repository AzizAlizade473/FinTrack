package com.financetracker.interfaces;

/**
 * Interface for objects that can be tracked in the finance system.
 * Provides methods to track activity and retrieve a summary.
 */
public interface Trackable {
    /**
     * Tracks the current state of the object.
     * @return a string describing the tracked state
     */
    String track();

    /**
     * Returns a summary of the tracked object.
     * @return summary string
     */
    String getSummary();
}

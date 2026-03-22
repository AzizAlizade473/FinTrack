package com.financetracker.server;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple JSON helper utility for building and parsing JSON strings.
 * No external libraries — all manual string manipulation.
 */
public class JsonHelper {

    /**
     * Parses a simple flat JSON string into a map of key-value pairs.
     * Handles strings and numbers. Does NOT handle nested objects or arrays.
     * @param json the JSON string to parse
     * @return map of key-value pairs
     */
    public static Map<String, String> parseJson(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null || json.trim().isEmpty()) return map;

        // Remove outer braces
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);

        // Split by comma, but respect quoted strings
        String[] pairs = splitJsonPairs(json);
        for (String pair : pairs) {
            int colonIndex = pair.indexOf(':');
            if (colonIndex < 0) continue;
            String key = pair.substring(0, colonIndex).trim();
            String value = pair.substring(colonIndex + 1).trim();

            // Remove quotes from key
            key = removeQuotes(key);
            // Remove quotes from value
            value = removeQuotes(value);

            map.put(key, value);
        }
        return map;
    }

    /**
     * Splits a JSON body into key:value pairs, respecting quoted strings.
     * @param json the inner JSON content (without outer braces)
     * @return array of key:value pair strings
     */
    private static String[] splitJsonPairs(String json) {
        java.util.List<String> pairs = new java.util.ArrayList<>();
        int depth = 0;
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (!inQuotes) {
                if (c == '{' || c == '[') depth++;
                if (c == '}' || c == ']') depth--;
                if (c == ',' && depth == 0) {
                    pairs.add(current.toString());
                    current = new StringBuilder();
                    continue;
                }
            }
            current.append(c);
        }
        if (current.length() > 0) {
            pairs.add(current.toString());
        }
        return pairs.toArray(new String[0]);
    }

    /**
     * Removes surrounding double quotes from a string.
     * @param s the string to unquote
     * @return the unquoted string
     */
    private static String removeQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    // ====================== JSON BUILDING METHODS ======================

    /**
     * Builds a success JSON response with data.
     * @param dataJson the JSON string for the data field
     * @return formatted JSON string
     */
    public static String successResponse(String dataJson) {
        return "{\"status\":\"success\",\"data\":" + dataJson + "}";
    }

    /**
     * Builds an error JSON response with a message.
     * @param message the error message
     * @return formatted JSON string
     */
    public static String errorResponse(String message) {
        return "{\"status\":\"error\",\"message\":\"" + escapeJson(message) + "\"}";
    }

    /**
     * Converts a User object to a JSON string.
     * @param userId the user's id
     * @param name the user's name
     * @param email the user's email
     * @return JSON representation
     */
    public static String userToJson(String userId, String name, String email) {
        return "{\"userId\":\"" + escapeJson(userId) + "\","
                + "\"name\":\"" + escapeJson(name) + "\","
                + "\"email\":\"" + escapeJson(email) + "\"}";
    }

    /**
     * Converts a transaction to a JSON string.
     * @param id transaction id
     * @param type INCOME or EXPENSE
     * @param amount the amount
     * @param date the date
     * @param description the description
     * @param category the category
     * @param source the source (for income)
     * @return JSON representation
     */
    public static String transactionToJson(String id, String type, double amount,
                                           String date, String description,
                                           String category, String source) {
        return "{\"id\":\"" + escapeJson(id) + "\","
                + "\"type\":\"" + escapeJson(type) + "\","
                + "\"amount\":" + amount + ","
                + "\"date\":\"" + escapeJson(date) + "\","
                + "\"description\":\"" + escapeJson(description) + "\","
                + "\"category\":\"" + escapeJson(category) + "\","
                + "\"source\":\"" + escapeJson(source) + "\"}";
    }

    /**
     * Converts a budget to a JSON string.
     * @param category the budget category
     * @param limit the spending limit
     * @param spent the amount spent
     * @param exceeded whether the budget is exceeded
     * @param remaining the remaining budget
     * @return JSON representation
     */
    public static String budgetToJson(String category, double limit, double spent,
                                      boolean exceeded, double remaining) {
        return "{\"category\":\"" + escapeJson(category) + "\","
                + "\"limit\":" + limit + ","
                + "\"spent\":" + spent + ","
                + "\"exceeded\":" + exceeded + ","
                + "\"remaining\":" + remaining + "}";
    }

    /**
     * Wraps a list of JSON strings into a JSON array.
     * @param items list of JSON strings
     * @return JSON array string
     */
    public static String toJsonArray(List<String> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i));
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Wraps a list of plain strings into a JSON array of strings.
     * @param items list of strings
     * @return JSON array of quoted strings
     */
    public static String stringsToJsonArray(List<String> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append("\"").append(escapeJson(items.get(i))).append("\"");
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Escapes special characters in a string for JSON output.
     * @param s the string to escape
     * @return escaped string
     */
    public static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

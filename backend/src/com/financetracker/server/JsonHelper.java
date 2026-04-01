package com.financetracker.server;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonHelper {

    public static Map<String, String> parseJson(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null || json.trim().isEmpty()) return map;

        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);

        String[] pairs = splitJsonPairs(json);
        for (String pair : pairs) {
            int colonIndex = pair.indexOf(':');
            if (colonIndex < 0) continue;
            String key = pair.substring(0, colonIndex).trim();
            String value = pair.substring(colonIndex + 1).trim();
            key = removeQuotes(key);
            value = removeQuotes(value);
            map.put(key, value);
        }
        return map;
    }

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

    private static String removeQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    public static String successResponse(String dataJson) {
        return "{\"status\":\"success\",\"data\":" + dataJson + "}";
    }

    public static String errorResponse(String message) {
        return "{\"status\":\"error\",\"message\":\"" + escapeJson(message) + "\"}";
    }

    public static String userToJson(String userId, String name, String email) {
        return "{\"userId\":\"" + escapeJson(userId) + "\","
                + "\"name\":\"" + escapeJson(name) + "\","
                + "\"email\":\"" + escapeJson(email) + "\"}";
    }

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

    public static String budgetToJson(String category, double limit, double spent,
                                      boolean exceeded, double remaining) {
        return "{\"category\":\"" + escapeJson(category) + "\","
                + "\"limit\":" + limit + ","
                + "\"spent\":" + spent + ","
                + "\"exceeded\":" + exceeded + ","
                + "\"remaining\":" + remaining + "}";
    }

    public static String toJsonArray(List<String> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i));
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String stringsToJsonArray(List<String> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append("\"").append(escapeJson(items.get(i))).append("\"");
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

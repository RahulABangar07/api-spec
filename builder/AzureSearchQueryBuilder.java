package com.example.filter;

import java.util.List;
import java.util.stream.Collectors;

public class AzureSearchQueryBuilder {

    public static String buildQuery(List<FilterDetail> filters) {
        if (filters == null || filters.isEmpty()) return "";

        StringBuilder query = new StringBuilder();

        for (int i = 0; i < filters.size(); i++) {
            FilterDetail f = filters.get(i);
            query.append(toExpression(f));

            if (i < filters.size() - 1) {
                // Lowercase AND/OR per OData spec
                query.append(" ").append(f.getLogicalOperator().toLowerCase()).append(" ");
            }
        }

        return "(" + query.toString() + ")";
    }

    private static String toExpression(FilterDetail filter) {
        String field = filter.getFieldName();
        String op = filter.getOperator().toUpperCase();

        switch (op) {
            case "=":
                return "(" + field + " eq '" + filter.getValues().get(0) + "')";
            case "!=":
                return "(" + field + " ne '" + filter.getValues().get(0) + "')";
            case ">":
                return "(" + field + " gt " + filter.getValues().get(0) + ")";
            case "<":
                return "(" + field + " lt " + filter.getValues().get(0) + ")";
            case ">=":
                return "(" + field + " ge " + filter.getValues().get(0) + ")";
            case "<=":
                return "(" + field + " le " + filter.getValues().get(0) + ")";
            case "IN":
                String inList = filter.getValues().stream()
                        .map(v -> "'" + v + "'")
                        .collect(Collectors.joining(", "));
                return "(" + field + " in (" + inList + "))";
            case "NOT IN":
                String notInList = filter.getValues().stream()
                        .map(v -> "'" + v + "'")
                        .collect(Collectors.joining(", "));
                // Azure Search doesn't support NOT IN directly
                return "(not (" + field + " in (" + notInList + ")))";
            default:
                return "(" + field + " eq '" + filter.getValues().get(0) + "')";
        }
    }
}

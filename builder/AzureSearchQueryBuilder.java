package com.example.filter;

import java.util.List;
import java.util.stream.Collectors;

public class AzureSearchQueryBuilder {

    public static String buildQuery(List<FilterDetail> filters) {
        if (filters == null || filters.isEmpty()) return "";

        StringBuilder query = new StringBuilder();

        for (int i = 0; i < filters.size(); i++) {
            FilterDetail f = filters.get(i);
            query.append(toODataExpression(f));

            if (i < filters.size() - 1) {
                // AND has precedence over OR; convert logicalOperator to lowercase for OData
                query.append(" ").append(f.getLogicalOperator().toLowerCase()).append(" ");
            }
        }

        return "(" + query.toString() + ")";
    }

    private static String toODataExpression(FilterDetail filter) {
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
                // OData uses `in` keyword for multiple values
                String inList = filter.getValues().stream()
                        .map(v -> "'" + v + "'")
                        .collect(Collectors.joining(", "));
                return "(" + field + " in (" + inList + "))";
            case "NOT IN":
                // Azure Search does not support NOT IN directly, use not (field in (...))
                String notInList = filter.getValues().stream()
                        .map(v -> "'" + v + "'")
                        .collect(Collectors.joining(", "));
                return "(not (" + field + " in (" + notInList + ")))";
            default:
                // fallback to equality
                return "(" + field + " eq '" + filter.getValues().get(0) + "')";
        }
    }
}

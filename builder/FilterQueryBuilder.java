package com.example.filter;

import java.util.List;
import java.util.stream.Collectors;

public class FilterQueryBuilder {

    // Convert to Cosmos DB SQL filter
    public static String toCosmosQuery(List<FilterDetail> filters) {
        return buildQuery(filters, true);
    }

    // Convert to Azure Search OData filter
    public static String toAzureSearchQuery(List<FilterDetail> filters) {
        return buildQuery(filters, false);
    }

    private static String buildQuery(List<FilterDetail> filters, boolean cosmos) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }

        // Build expressions
        List<String> expressions = filters.stream()
            .map(f -> toExpression(f, cosmos))
            .collect(Collectors.toList());

        // Apply AND precedence over OR
        String combined = String.join(" ", expressions);
        combined = combined.replaceAll("AND", ") AND (");
        combined = combined.replaceAll("OR", ") OR (");

        return "(" + combined + ")";
    }

    private static String toExpression(FilterDetail filter, boolean cosmos) {
        String field = filter.getFieldName();
        String operator = filter.getOperator().toUpperCase();
        List<String> vals = filter.getValues();

        String valueExpr;
        if ("IN".equals(operator) || "NOT IN".equals(operator)) {
            String joined = vals.stream()
                .map(v -> "'" + v + "'")
                .collect(Collectors.joining(", "));
            valueExpr = operator + " (" + joined + ")";
            return "(" + field + " " + valueExpr + ")";
        } else {
            String val = "'" + vals.get(0) + "'";
            return "(" + field + " " + operator + " " + val + ")";
        }
    }
}

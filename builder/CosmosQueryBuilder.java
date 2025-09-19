package com.example.filter;

import java.util.List;
import java.util.stream.Collectors;

public class CosmosQueryBuilder {

    public static String buildQuery(List<FilterDetail> filters) {
        if (filters == null || filters.isEmpty()) return "";

        StringBuilder query = new StringBuilder();

        for (int i = 0; i < filters.size(); i++) {
            FilterDetail f = filters.get(i);
            query.append(toExpression(f));

            if (i < filters.size() - 1) {
                query.append(" ").append(f.getLogicalOperator()).append(" ");
            }
        }

        return "(" + query.toString() + ")";
    }

    private static String toExpression(FilterDetail filter) {
        String field = filter.getFieldName();
        String op = filter.getOperator().toUpperCase();

        if ("IN".equals(op) || "NOT IN".equals(op)) {
            String joined = filter.getValues().stream()
                    .map(v -> "'" + v + "'")
                    .collect(Collectors.joining(", "));
            return "(" + field + " " + op + " (" + joined + "))";
        } else {
            return "(" + field + " " + op + " '" + filter.getValues().get(0) + "')";
        }
    }
}

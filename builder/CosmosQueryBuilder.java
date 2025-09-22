package com.example.search.builder;

import java.util.List;
import java.util.stream.Collectors;

public class CosmosQueryBuilder {

    public static String buildQuery(Filter filter) {
        String whereClause = buildFilterExpression(filter);
        return "SELECT * FROM c WHERE " + whereClause;
    }

    private static String buildFilterExpression(Filter filter) {
        String expr;

        if (filter.isGroup()) {
            String joined = filter.getChildren().stream()
                    .map(CosmosQueryBuilder::buildFilterExpression)
                    .collect(Collectors.joining(" " + filter.getLogicalOperator().name() + " "));

            expr = "(" + joined + ")";
        } else {
            expr = buildSingleCondition(filter);
        }

        // Apply NOT if needed
        if (filter.isNegate()) {
            expr = "NOT " + expr;
        }

        return expr;
    }

    private static String buildSingleCondition(Filter filter) {
        String field = "c." + filter.getField();
        Object value = filter.getValue();

        switch (filter.getOperator()) {
            case EQ:
                return String.format("%s = %s", field, formatValue(value));
            case NE:
                return String.format("%s != %s", field, formatValue(value));
            case LT:
                return String.format("%s < %s", field, formatValue(value));
            case LTE:
                return String.format("%s <= %s", field, formatValue(value));
            case GT:
                return String.format("%s > %s", field, formatValue(value));
            case GTE:
                return String.format("%s >= %s", field, formatValue(value));
            case IN:
                return String.format("%s IN (%s)", field, formatList(value));
            case NOT_IN:
                return String.format("%s NOT IN (%s)", field, formatList(value));
            default:
                throw new IllegalArgumentException("Unsupported operator: " + filter.getOperator());
        }
    }

    private static String formatValue(Object value) {
        if (value instanceof String) {
            return "'" + value + "'";
        }
        return value.toString();
    }

    private static String formatList(Object value) {
        if (!(value instanceof List<?>)) {
            throw new IllegalArgumentException("IN operator requires a List value");
        }
        return ((List<?>) value).stream()
                .map(CosmosQueryBuilder::formatValue)
                .collect(Collectors.joining(", "));
    }
}

package com.example.search.builder;

import com.azure.search.documents.models.SearchOptions;

import java.util.List;
import java.util.stream.Collectors;

public class AzureSearchQueryBuilder {

    public static String buildFilterExpression(Filter filter) {
        String expr;

        if (filter.isGroup()) {
            String joined = filter.getChildren().stream()
                    .map(AzureSearchQueryBuilder::buildFilterExpression)
                    .collect(Collectors.joining(" " + filter.getLogicalOperator().name().toLowerCase() + " "));
            expr = "(" + joined + ")";
        } else {
            expr = buildSingleCondition(filter);
        }

        // Apply NOT if needed
        if (filter.isNegate()) {
            expr = "not " + expr;
        }

        return expr;
    }

    private static String buildSingleCondition(Filter filter) {
        String field = filter.getField();
        Object value = filter.getValue();

        switch (filter.getOperator()) {
            case EQ:
                return String.format("%s eq %s", field, formatValue(value));
            case NE:
                return String.format("%s ne %s", field, formatValue(value));
            case LT:
                return String.format("%s lt %s", field, formatValue(value));
            case LTE:
                return String.format("%s le %s", field, formatValue(value));
            case GT:
                return String.format("%s gt %s", field, formatValue(value));
            case GTE:
                return String.format("%s ge %s", field, formatValue(value));
            case IN:
                return String.format("%s in (%s)", field, formatList(value));
            case NOT_IN:
                return String.format("not (%s in (%s))", field, formatList(value));
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
                .map(AzureSearchQueryBuilder::formatValue)
                .collect(Collectors.joining(", "));
    }

    public static SearchOptions buildSearchOptions(Filter filter) {
        String filterExpr = buildFilterExpression(filter);
        return new SearchOptions().setFilter(filterExpr);
    }
}

package com.example.search.builder;

import java.util.List;

public class Filter {

    private String field;                // null if it's a group
    private Operator operator;           // null if it's a group
    private Object value;                // null if it's a group
    private LogicalOperator logicalOperator; // AND / OR
    private List<Filter> children;       // for nested groups
    private boolean negate;              // if true => wrap in NOT

    public enum Operator {
        EQ, NE, LT, LTE, GT, GTE, IN, NOT_IN
    }

    public enum LogicalOperator {
        AND, OR
    }

    // Constructor for simple condition
    public Filter(String field, Operator operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    // Constructor for simple condition with negation
    public Filter(String field, Operator operator, Object value, boolean negate) {
        this(field, operator, value);
        this.negate = negate;
    }

    // Constructor for group
    public Filter(List<Filter> children, LogicalOperator logicalOperator) {
        this.children = children;
        this.logicalOperator = logicalOperator;
    }

    // Constructor for group with negation
    public Filter(List<Filter> children, LogicalOperator logicalOperator, boolean negate) {
        this(children, logicalOperator);
        this.negate = negate;
    }

    // Getters
    public String getField() { return field; }
    public Operator getOperator() { return operator; }
    public Object getValue() { return value; }
    public LogicalOperator getLogicalOperator() { return logicalOperator; }
    public List<Filter> getChildren() { return children; }
    public boolean isNegate() { return negate; }

    public boolean isGroup() { return children != null && !children.isEmpty(); }
}

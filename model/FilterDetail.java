package com.example.filter;

import java.util.List;

public class FilterDetail {
    private String fieldName;          // e.g. "category"
    private String operator;           // e.g. "=", "IN", "NOT IN"
    private List<String> values;       // e.g. ["Books", "Electronics"]
    private String logicalOperator;    // e.g. "AND", "OR"

    // Getters & setters
    public String getFieldName() {
        return fieldName;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOperator() {
        return operator;
    }
    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<String> getValues() {
        return values;
    }
    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getLogicalOperator() {
        return logicalOperator;
    }
    public void setLogicalOperator(String logicalOperator) {
        this.logicalOperator = logicalOperator;
    }
}

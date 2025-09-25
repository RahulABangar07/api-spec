import java.util.List;
import java.util.stream.Collectors;

public class FilterBuilder {

    // Supported operators
    public enum Operator {
        EQUALS("=", "eq"),
        NOT_EQUALS("!=", "ne"),
        GREATER_THAN(">", "gt"),
        LESS_THAN("<", "lt"),
        GREATER_OR_EQUAL(">=", "ge"),
        LESS_OR_EQUAL("<=", "le"),
        IN("IN", "in"),
        NOT_IN("NOT IN", "not in");

        private final String readable;
        private final String azure;

        Operator(String readable, String azure) {
            this.readable = readable;
            this.azure = azure;
        }

        public String getReadable() { return readable; }
        public String getAzure() { return azure; }
    }

    // Logical operators
    public enum Logical {
        AND("AND", "and"),
        OR("OR", "or"),
        NOT("NOT", "not");

        private final String readable;
        private final String azure;

        Logical(String readable, String azure) {
            this.readable = readable;
            this.azure = azure;
        }

        public String getReadable() { return readable; }
        public String getAzure() { return azure; }
    }

    // Base interface
    public interface Filter {}

    // Simple filter condition
    public static class Condition implements Filter {
        private final String field;
        private final Operator operator;
        private final Object value;
        private final boolean isArrayField; // NEW: for array handling

        public Condition(String field, Operator operator, Object value) {
            this(field, operator, value, false);
        }

        public Condition(String field, Operator operator, Object value, boolean isArrayField) {
            this.field = field;
            this.operator = operator;
            this.value = value;
            this.isArrayField = isArrayField;
        }

        public String getField() { return field; }
        public Operator getOperator() { return operator; }
        public Object getValue() { return value; }
        public boolean isArrayField() { return isArrayField; }
    }

    // Composite filter
    public static class CompositeFilter implements Filter {
        private final Logical logical;
        private final List<Filter> filters;

        public CompositeFilter(Logical logical, List<Filter> filters) {
            this.logical = logical;
            this.filters = filters;
        }

        public Logical getLogical() { return logical; }
        public List<Filter> getFilters() { return filters; }
    }

    /* ---------------- Human-readable builder ---------------- */
    public static String buildReadable(Filter filter) {
        if (filter instanceof Condition condition) {
            return buildReadableCondition(condition);
        } else if (filter instanceof CompositeFilter composite) {
            return buildReadableComposite(composite);
        }
        return "";
    }

    private static String buildReadableCondition(Condition condition) {
        if (condition.getOperator() == Operator.IN || condition.getOperator() == Operator.NOT_IN) {
            if (condition.getValue() instanceof List<?> list) {
                String values = list.stream()
                        .map(v -> "'" + v + "'")
                        .collect(Collectors.joining(", "));
                return condition.getField() + " " + condition.getOperator().getReadable() + " (" + values + ")";
            }
        }
        return condition.getField() + " " + condition.getOperator().getReadable() + " '" + condition.getValue() + "'";
    }

    private static String buildReadableComposite(CompositeFilter composite) {
        if (composite.getLogical() == Logical.NOT && composite.getFilters().size() == 1) {
            return "NOT (" + buildReadable(composite.getFilters().get(0)) + ")";
        }
        String joined = composite.getFilters().stream()
                .map(FilterBuilder::buildReadable)
                .collect(Collectors.joining(" " + composite.getLogical().getReadable() + " "));
        return "(" + joined + ")";
    }

    /* ---------------- Azure Search builder ---------------- */
    public static String buildAzure(Filter filter) {
        if (filter instanceof Condition condition) {
            return buildAzureCondition(condition);
        } else if (filter instanceof CompositeFilter composite) {
            return buildAzureComposite(composite);
        }
        return "";
    }

    private static String buildAzureCondition(Condition condition) {
        String field = condition.getField();
        String op = condition.getOperator().getAzure();
        Object value = condition.getValue();

        // Handle array fields with "any()"
        if (condition.isArrayField()) {
            String var = "x"; // array iteration variable
            if (condition.getOperator() == Operator.IN || condition.getOperator() == Operator.NOT_IN) {
                if (value instanceof List<?> list) {
                    String expr = list.stream()
                            .map(v -> var + " eq " + formatAzureValue(v))
                            .collect(Collectors.joining(" or "));
                    String full = field + "/any(" + var + ": " + expr + ")";
                    return (condition.getOperator() == Operator.NOT_IN) ? "not (" + full + ")" : full;
                }
            } else {
                return field + "/any(" + var + ": " + var + " " + op + " " + formatAzureValue(value) + ")";
            }
        }

        // Normal (non-array field)
        if (condition.getOperator() == Operator.IN || condition.getOperator() == Operator.NOT_IN) {
            if (value instanceof List<?> list) {
                String values = list.stream()
                        .map(FilterBuilder::formatAzureValue)
                        .collect(Collectors.joining(", "));
                String expr = field + " in (" + values + ")";
                return (condition.getOperator() == Operator.NOT_IN) ? "not (" + expr + ")" : expr;
            }
        }

        return field + " " + op + " " + formatAzureValue(value);
    }

    private static String buildAzureComposite(CompositeFilter composite) {
        if (composite.getLogical() == Logical.NOT && composite.getFilters().size() == 1) {
            return "not (" + buildAzure(composite.getFilters().get(0)) + ")";
        }
        String joined = composite.getFilters().stream()
                .map(FilterBuilder::buildAzure)
                .collect(Collectors.joining(" " + composite.getLogical().getAzure() + " "));
        return "(" + joined + ")";
    }

    private static String formatAzureValue(Object value) {
        if (value instanceof Number) {
            return value.toString();
        }
        return "'" + value + "'";
    }

    /* ---------------- Example usage ---------------- */
    public static void main(String[] args) {
        Filter filter = new CompositeFilter(Logical.AND, List.of(
                new Condition("age", Operator.GREATER_OR_EQUAL, 18),
                new CompositeFilter(Logical.OR, List.of(
                        new Condition("country", Operator.EQUALS, "India"),
                        new Condition("country", Operator.EQUALS, "US")
                )),
                new Condition("tags", Operator.IN, List.of("sports", "music"), true), // ARRAY field
                new CompositeFilter(Logical.NOT, List.of(
                        new Condition("status", Operator.EQUALS, "inactive")
                ))
        ));

        System.out.println("Readable: " + buildReadable(filter));
        System.out.println("Azure: " + buildAzure(filter));
    }
}

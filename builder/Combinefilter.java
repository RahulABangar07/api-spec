import java.util.List;
import java.util.stream.Collectors;

public class FilterModel {

    // Operators
    public enum Operator {
        EQUALS("=", "eq"),
        NOT_EQUALS("!=", "ne"),
        GREATER_THAN(">", "gt"),
        LESS_THAN("<", "lt"),
        GREATER_OR_EQUAL(">=", "ge"),
        LESS_OR_EQUAL("<=", "le"),
        IN("IN", "in"),
        NOT_IN("NOT IN", "not in");

        private final String cosmos;
        private final String azure;

        Operator(String cosmos, String azure) {
            this.cosmos = cosmos;
            this.azure = azure;
        }

        public String getCosmos() { return cosmos; }
        public String getAzure() { return azure; }
    }

    // Logical operators
    public enum Logical {
        AND("AND", "and"),
        OR("OR", "or"),
        NOT("NOT", "not");

        private final String cosmos;
        private final String azure;

        Logical(String cosmos, String azure) {
            this.cosmos = cosmos;
            this.azure = azure;
        }

        public String getCosmos() { return cosmos; }
        public String getAzure() { return azure; }
    }

    // Filter base
    public interface Filter {}

    // Simple condition
    public static class Condition implements Filter {
        private final String field;
        private final Operator operator;
        private final Object value;
        private final boolean isArrayField;

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

    // Composite (AND/OR/NOT)
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

    /* ---------------- Azure Search Builder ---------------- */
    public static class AzureSearchQueryBuilder {

        public static String build(Filter filter) {
            if (filter instanceof Condition condition) {
                return buildCondition(condition);
            } else if (filter instanceof CompositeFilter composite) {
                return buildComposite(composite);
            }
            return "";
        }

        private static String buildCondition(Condition condition) {
            String field = condition.getField();
            String op = condition.getOperator().getAzure();
            Object value = condition.getValue();

            if (condition.isArrayField()) {
                String var = "x";
                if (value instanceof List<?> list) {
                    String expr = list.stream()
                            .map(v -> var + " eq " + formatValue(v))
                            .collect(Collectors.joining(" or "));
                    String full = field + "/any(" + var + ": " + expr + ")";
                    return (condition.getOperator() == Operator.NOT_IN) ? "not (" + full + ")" : full;
                }
                return field + "/any(" + var + ": " + var + " " + op + " " + formatValue(value) + ")";
            }

            if (condition.getOperator() == Operator.IN || condition.getOperator() == Operator.NOT_IN) {
                if (value instanceof List<?> list) {
                    String values = list.stream()
                            .map(AzureSearchQueryBuilder::formatValue)
                            .collect(Collectors.joining(", "));
                    String expr = field + " in (" + values + ")";
                    return (condition.getOperator() == Operator.NOT_IN) ? "not (" + expr + ")" : expr;
                }
            }

            return field + " " + op + " " + formatValue(value);
        }

        private static String buildComposite(CompositeFilter composite) {
            if (composite.getLogical() == Logical.NOT && composite.getFilters().size() == 1) {
                return "not (" + build(composite.getFilters().get(0)) + ")";
            }
            return "(" + composite.getFilters().stream()
                    .map(AzureSearchQueryBuilder::build)
                    .collect(Collectors.joining(" " + composite.getLogical().getAzure() + " ")) + ")";
        }

        private static String formatValue(Object value) {
            return (value instanceof Number) ? value.toString() : "'" + value + "'";
        }
    }

    /* ---------------- Cosmos DB Query Builder ---------------- */
    public static class CosmosQueryBuilder {

        public static String build(Filter filter) {
            if (filter instanceof Condition condition) {
                return buildCondition(condition);
            } else if (filter instanceof CompositeFilter composite) {
                return buildComposite(composite);
            }
            return "";
        }

        private static String buildCondition(Condition condition) {
            String field = "c." + condition.getField();
            Object value = condition.getValue();

            if (condition.isArrayField()) {
                if (value instanceof List<?> list) {
                    String expr = list.stream()
                            .map(v -> "ARRAY_CONTAINS(" + field + ", " + formatValue(v) + ")")
                            .collect(Collectors.joining(" OR "));
                    String full = "(" + expr + ")";
                    return (condition.getOperator() == Operator.NOT_IN)
                            ? "NOT " + full
                            : full;
                }
                return "ARRAY_CONTAINS(" + field + ", " + formatValue(value) + ")";
            }

            if (condition.getOperator() == Operator.IN || condition.getOperator() == Operator.NOT_IN) {
                if (value instanceof List<?> list) {
                    String values = list.stream()
                            .map(CosmosQueryBuilder::formatValue)
                            .collect(Collectors.joining(", "));
                    String expr = field + " IN (" + values + ")";
                    return (condition.getOperator() == Operator.NOT_IN) ? "NOT " + expr : expr;
                }
            }

            return field + " " + condition.getOperator().getCosmos() + " " + formatValue(value);
        }

        private static String buildComposite(CompositeFilter composite) {
            if (composite.getLogical() == Logical.NOT && composite.getFilters().size() == 1) {
                return "NOT (" + build(composite.getFilters().get(0)) + ")";
            }
            return "(" + composite.getFilters().stream()
                    .map(CosmosQueryBuilder::build)
                    .collect(Collectors.joining(" " + composite.getLogical().getCosmos() + " ")) + ")";
        }

        private static String formatValue(Object value) {
            return (value instanceof Number) ? value.toString() : "'" + value + "'";
        }
    }

    /* ---------------- Example ---------------- */
    public static void main(String[] args) {
        Filter filter = new CompositeFilter(Logical.AND, List.of(
                new Condition("age", Operator.GREATER_OR_EQUAL, 18),
                new CompositeFilter(Logical.OR, List.of(
                        new Condition("country", Operator.EQUALS, "India"),
                        new Condition("country", Operator.EQUALS, "US")
                )),
                new Condition("tags", Operator.IN, List.of("sports", "music"), true), // array field
                new CompositeFilter(Logical.NOT, List.of(
                        new Condition("status", Operator.EQUALS, "inactive")
                ))
        ));

        System.out.println("Azure Search Filter:");
        System.out.println(AzureSearchQueryBuilder.build(filter));

        System.out.println("\nCosmos DB Filter:");
        System.out.println(CosmosQueryBuilder.build(filter));
    }
}

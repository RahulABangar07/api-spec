import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CosmosQueryBuilder {

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\(|\\)|\\bAND\\b|\\bOR\\b|\\bNOT\\b|[<>]=?|!=|=|\\bIN\\b|\\bNOT IN\\b|\\bLIKE\\b|\\w+|'.*?'|\\d+\\.\\d+|\\d+|,",
            Pattern.CASE_INSENSITIVE
    );

    public static String buildCosmosQuery(String userExpression, String alias) {
        String normalized = normalizeExpression(userExpression);
        String translated = translateTokens(normalized, alias);
        return "SELECT * FROM " + alias + " WHERE " + translated;
    }

    // Step 1: Normalize spacing
    private static String normalizeExpression(String expr) {
        expr = expr.replaceAll("\\s+", " "); // collapse multiple spaces
        return expr.trim();
    }

    // Step 2: Token translation
    private static String translateTokens(String expr, String alias) {
        Matcher matcher = TOKEN_PATTERN.matcher(expr);
        StringBuilder sb = new StringBuilder();

        boolean inValueList = false;   // track if inside IN (...)
        String lastToken = null;

        while (matcher.find()) {
            String token = matcher.group();

            if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR") || token.equalsIgnoreCase("NOT")) {
                sb.append(token.toUpperCase()).append(" ");
            } else if (token.equals("(")) {
                sb.append("(").append(" ");
                if ("IN".equalsIgnoreCase(lastToken)) {
                    inValueList = true;
                }
            } else if (token.equals(")")) {
                sb.append(")").append(" ");
                inValueList = false;
            } else if (isOperator(token)) {
                sb.append(token.toUpperCase()).append(" ");
            } else if (token.equals(",")) {
                sb.append(", ");
            } else if (isNumber(token) || isBoolean(token)) {
                sb.append(token).append(" ");
            } else if (isQuoted(token)) {
                sb.append(token).append(" ");
            } else {
                if (lastToken != null && isOperator(lastToken)) {
                    // VALUE after operator
                    if (looksLikeNumber(token) || isBoolean(token)) {
                        sb.append(token).append(" ");
                    } else {
                        sb.append("'").append(token).append("' ");
                    }
                } else if (inValueList) {
                    // VALUES inside IN (...)
                    if (looksLikeNumber(token) || isBoolean(token)) {
                        sb.append(token).append(" ");
                    } else {
                        sb.append("'").append(token).append("' ");
                    }
                } else {
                    // FIELD name
                    sb.append(alias).append(".").append(token).append(" ");
                }
            }
            lastToken = token;
        }
        return sb.toString().trim();
    }

    private static boolean isOperator(String token) {
        return Arrays.asList("=", "!=", "<", "<=", ">", ">=", "IN", "NOT", "NOT IN", "LIKE")
                .contains(token.toUpperCase());
    }

    private static boolean isNumber(String token) {
        return token.matches("\\d+(\\.\\d+)?([lLdDfF])?");
    }

    private static boolean looksLikeNumber(String token) {
        return token.matches("\\d+(\\.\\d+)?");
    }

    private static boolean isBoolean(String token) {
        return token.equalsIgnoreCase("true") || token.equalsIgnoreCase("false");
    }

    private static boolean isQuoted(String token) {
        return token.startsWith("'") && token.endsWith("'");
    }

    // Demo
    public static void main(String[] args) {
        String input = "((age > 80 and city IN (Pune, Mumbai)) or (pin = 52356 and work=Pune)) and maritalstatus=Married and status NOT IN (Active, Blocked)";

        String cosmosQuery = buildCosmosQuery(input, "c");

        System.out.println("Input: " + input);
        System.out.println("Cosmos SQL: " + cosmosQuery);
    }
}

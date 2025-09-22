import com.example.search.builder.Filter;
import com.example.search.builder.AzureSearchQueryBuilder;

import java.util.Arrays;

public class Example {
    public static void main(String[] args) {
        // (price gt 100 and price lt 500)
        Filter priceGroup = new Filter(
                Arrays.asList(
                        new Filter("price", Filter.Operator.GT, 100),
                        new Filter("price", Filter.Operator.LT, 500)
                ),
                Filter.LogicalOperator.AND
        );

        // not(category eq 'Books' and rating ge 4)
        Filter categoryGroup = new Filter(
                Arrays.asList(
                        new Filter("category", Filter.Operator.EQ, "Books"),
                        new Filter("rating", Filter.Operator.GTE, 4)
                ),
                Filter.LogicalOperator.AND,
                true // apply NOT
        );

        // Combine with OR
        Filter root = new Filter(
                Arrays.asList(priceGroup, categoryGroup),
                Filter.LogicalOperator.OR
        );

        String filterExpr = AzureSearchQueryBuilder.buildFilterExpression(root);
        System.out.println("Generated Filter: " + filterExpr);

        // Output:
        // (price gt 100 and price lt 500) or not (category eq 'Books' and rating ge 4)
    }
}

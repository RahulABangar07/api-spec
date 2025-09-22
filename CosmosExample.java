import com.example.search.builder.Filter;
import com.example.search.builder.CosmosQueryBuilder;

import java.util.Arrays;

public class CosmosExample {
    public static void main(String[] args) {
        // (c.price > 100 AND c.price < 500)
        Filter priceGroup = new Filter(
                Arrays.asList(
                        new Filter("price", Filter.Operator.GT, 100),
                        new Filter("price", Filter.Operator.LT, 500)
                ),
                Filter.LogicalOperator.AND
        );

        // NOT (c.category = 'Books' AND c.rating >= 4)
        Filter categoryGroup = new Filter(
                Arrays.asList(
                        new Filter("category", Filter.Operator.EQ, "Books"),
                        new Filter("rating", Filter.Operator.GTE, 4)
                ),
                Filter.LogicalOperator.AND,
                true // negate
        );

        // Combine with OR
        Filter root = new Filter(
                Arrays.asList(priceGroup, categoryGroup),
                Filter.LogicalOperator.OR
        );

        String query = CosmosQueryBuilder.buildQuery(root);
        System.out.println("Generated Cosmos Query: " + query);

        // Output:
        // SELECT * FROM c WHERE (c.price > 100 AND c.price < 500) OR NOT (c.category = 'Books' AND c.rating >= 4)
    }
}

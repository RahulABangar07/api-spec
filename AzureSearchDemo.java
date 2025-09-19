import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchPagedFlux;
import com.azure.search.documents.models.SearchResult;
import com.example.filter.*;

import java.util.Arrays;
import java.util.List;

public class AzureSearchDemo {
    public static void main(String[] args) {

        // Filters
        FilterDetail f1 = new FilterDetail();
        f1.setFieldName("category");
        f1.setOperator("IN");
        f1.setValues(Arrays.asList("Books", "Electronics"));
        f1.setLogicalOperator("AND");

        FilterDetail f2 = new FilterDetail();
        f2.setFieldName("price");
        f2.setOperator(">");
        f2.setValues(List.of("100"));
        f2.setLogicalOperator("OR");

        List<FilterDetail> filters = Arrays.asList(f1, f2);

        // Sort by fields
        List<String> orderBy = Arrays.asList("price desc", "rating asc");

        // Select only these fields
        List<String> selectFields = Arrays.asList("id", "category", "price", "rating");

        FilterGenerationService service = new FilterGenerationService();
        SearchOptions options = service.generateAzureSearchOptions(filters, orderBy, selectFields);

        // Initialize Azure Search client (already configured)
        SearchAsyncClient client = /* injected or created */;
        SearchPagedFlux results = client.search("*", options);

        // Subscribe to results
        results.subscribe(r -> {
            SearchResult result = r;
            System.out.println("Document: " + result.getDocument());
        });
    }
}

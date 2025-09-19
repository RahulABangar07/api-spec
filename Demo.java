import com.example.filter.*;
import java.util.Arrays;
import java.util.List;

public class Demo {
    public static void main(String[] args) {
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

        FilterGenerationService service = new FilterGenerationService();

        System.out.println("Cosmos DB Query: " + service.generateCosmosQuery(filters));
        System.out.println("Azure Search Query: " + service.generateAzureSearchQuery(filters));
    }
}

package com.example.filter;

import com.azure.search.documents.models.SearchOptions;
import java.util.List;

public class FilterGenerationService {

    // Cosmos DB query
    public String generateCosmosQuery(List<FilterDetail> filters) {
        return CosmosQueryBuilder.buildQuery(filters);
    }

    // Azure Search: build SearchOptions with filter
    public SearchOptions generateAzureSearchOptions(List<FilterDetail> filters) {
        String filter = AzureSearchQueryBuilder.buildQuery(filters);
        SearchOptions options = new SearchOptions();

        if (filter != null && !filter.isEmpty()) {
            options.setFilter(filter);
        }
        return options;
    }
}

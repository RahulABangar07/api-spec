package com.example.filter;

import com.azure.search.documents.models.SearchOptions;
import java.util.List;

public class FilterGenerationService {

    // Cosmos DB SQL query
    public String generateCosmosQuery(List<FilterDetail> filters) {
        return CosmosQueryBuilder.buildQuery(filters);
    }

    // Azure SearchOptions builder with filter, orderBy, select fields
    public SearchOptions generateAzureSearchOptions(
            List<FilterDetail> filters,
            List<String> orderByFields,
            List<String> selectFields) {

        SearchOptions options = new SearchOptions();

        // Set filter string
        String filter = AzureSearchQueryBuilder.buildQuery(filters);
        if (filter != null && !filter.isEmpty()) {
            options.setFilter(filter);
        }

        // Set orderBy if provided
        if (orderByFields != null && !orderByFields.isEmpty()) {
            options.setOrderBy(orderByFields);
        }

        // Set selected fields if provided
        if (selectFields != null && !selectFields.isEmpty()) {
            options.setSelect(selectFields);
        }

        return options;
    }
}

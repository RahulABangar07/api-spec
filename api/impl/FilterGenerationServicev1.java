package com.example.filter;

import java.util.List;

public class FilterGenerationServicev1 {

    public String generateCosmosQuery(List<FilterDetail> filters) {
        return CosmosQueryBuilder.buildQuery(filters);
    }

    public String generateAzureSearchQuery(List<FilterDetail> filters) {
        return AzureSearchQueryBuilder.buildQuery(filters);
    }
}

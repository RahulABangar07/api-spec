package com.example.filter;

import java.util.List;

public class FilterGenerationService {

    public String generateCosmosQuery(List<FilterDetail> filters) {
        return FilterQueryBuilder.toCosmosQuery(filters);
    }

    public String generateAzureSearchQuery(List<FilterDetail> filters) {
        return FilterQueryBuilder.toAzureSearchQuery(filters);
    }
}

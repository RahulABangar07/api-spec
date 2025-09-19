FilterGenerationService service = new FilterGenerationService();

SearchOptions options = service.generateAzureSearchOptions(
        filters,
        Arrays.asList("price desc", "rating asc"),
        Arrays.asList("id", "category", "price", "rating")
);

// Now options.getFilter() will contain a fully OData-compliant string
// AND / OR precedence and operator normalization are handled automatically

package com.example.campaign.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class CreateCampaignRequest {
    private String title;
    private String description;
    private List<String> reviewers;
    private OffsetDateTime publishedDate;
    private OffsetDateTime expirationDate;
    private FilterAggregator filterAggregator;

    // Getters and Setters
}

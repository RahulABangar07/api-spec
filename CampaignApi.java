package com.example.campaign.api;

import com.example.campaign.dto.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RequestMapping("/campaigns")
public interface CampaignApi {

    /**
     * Create a new campaign.
     */
    @PostMapping("/create")
    @ResponseBody
    CreateCampaignResponse createCampaign(@RequestBody CreateCampaignRequest request);

    /**
     * Get campaigns based on optional published and expiration dates.
     */
    @GetMapping
    @ResponseBody
    List<Campaign> getCampaigns(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        OffsetDateTime publishedDate,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        OffsetDateTime expirationDate,

        @RequestHeader("Authorization") String authorization,
        @RequestHeader("Product") String product
    );

    /**
     * Submit a campaign.
     */
    @PostMapping("/submit")
    @ResponseBody
    SubmittedCampaign submitCampaign(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        OffsetDateTime publishedDate,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        OffsetDateTime expirationDate,

        @RequestHeader("Authorization") String authorization,
        @RequestHeader("Product") String product
    );

    /**
     * Review a campaign.
     */
    @PostMapping("/review")
    @ResponseBody
    ReviewedCampaign reviewCampaign(
        @RequestBody ReviewCampaignRequest request,
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("Product") String product
    );
}

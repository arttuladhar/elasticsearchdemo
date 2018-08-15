package com.aayushtuladhar.demo.elasticsearchdemo.controller;

import com.aayushtuladhar.demo.elasticsearchdemo.domain.Listing;
import com.aayushtuladhar.demo.elasticsearchdemo.service.ESListingService;
import java.util.Collections;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ESListingController {

  ESListingService esListingService;

  public ESListingController(
      ESListingService esListingService) {
    this.esListingService = esListingService;
  }

  @GetMapping("/listing/{documentId}")
  public Listing getListings(@PathVariable String documentId) {
    return esListingService.getListing(documentId);
  }

  @GetMapping("/listings")
  public List<Listing> getListingsWithQuery(@RequestParam("query") String query, @RequestParam(value = "description", required = false) String description) {

    // Match Query
    if (query.equals("all")){
      return esListingService.getAllListings();
    }

    if (query.equals("sales")) {
      return esListingService.getAllSaleListings();
    }

    if (query.equals("housing-mn")) {
      return esListingService.getAllHousingListingsInMinnesota();
    }

    if (query.equals("desc")){
      return esListingService.getAllListingsWithDescription(description);
    }

    return Collections.emptyList();

  }
}

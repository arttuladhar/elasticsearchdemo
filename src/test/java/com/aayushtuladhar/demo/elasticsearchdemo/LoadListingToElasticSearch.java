package com.aayushtuladhar.demo.elasticsearchdemo;

import com.aayushtuladhar.demo.elasticsearchdemo.domain.Category;
import com.aayushtuladhar.demo.elasticsearchdemo.domain.Listing;
import com.aayushtuladhar.demo.elasticsearchdemo.service.ESListingService;
import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(value = "local")
public class LoadListingToElasticSearch {

  private static final String LISTING_FILENAME = "listings.csv";

  @Autowired
  ESListingService ESListingService;

  @Test
  public void loadListingToElasticSearch() {

    List<Listing> listings = readListingsFromCSV();

    listings.forEach(listing -> {
      System.out.println("Publishing Item to ElasticSearch");
      ESListingService.publishListing(listing);
    });

  }

  private List<Listing> readListingsFromCSV() {
    File csvFile;

    List<Listing> listingsToLoad = new ArrayList<>();

    try {
      csvFile = new ClassPathResource(LISTING_FILENAME).getFile();
      CSVReader csvReader = new CSVReader(new FileReader(csvFile));

      //Reading Line By Line
      String[] record = null;
      while ((record = csvReader.readNext()) != null) {
        Listing listing = Listing.builder()
            .listingId(record[0])
            .shortDescription(record[1])
            .description(record[2])
            .category(Category.valueOf(record[3]))
            .price(Double.valueOf(record[4]))
            .state(record[5])
            .zipCode(record[6])
            .createdDateEpocTS(record[7])
            .expiryDateEpochTS(record[8])
            .createdBy(record[9])
            .build();

        listingsToLoad.add(listing);
      }
    } catch (IOException e) {
      System.out.println("Error Reading File");
      System.out.println(e.getMessage());
    }

    return listingsToLoad;

  }


}

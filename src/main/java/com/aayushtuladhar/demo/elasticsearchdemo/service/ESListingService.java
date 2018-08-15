package com.aayushtuladhar.demo.elasticsearchdemo.service;

import static com.aayushtuladhar.demo.elasticsearchdemo.config.ElasticIndex.*;
import static com.aayushtuladhar.demo.elasticsearchdemo.domain.Category.HOUSING;

import com.aayushtuladhar.demo.elasticsearchdemo.config.ElasticIndex;
import com.aayushtuladhar.demo.elasticsearchdemo.domain.Listing;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.naming.directory.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MatchQuery;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.simple.JSONValue;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ESListingService {

  private static final String HEADER_ACCEPT = "accept";
  private static final String HEADER_CONTENT_TYPE = "application/json";

  private static final String CATEGORY_KEY = "category.keyword";
  private static final String STATE_KEY = "state.keyword";
  private static final String DESCRIPTION_KEY = "description";


  private static final String TAG = ESListingService.class.getCanonicalName() + "; ";

  private RestHighLevelClient restHighLevelClient;
  private ObjectMapper objectMapper;

  public ESListingService(RestHighLevelClient restHighLevelClient,
      ObjectMapper objectMapper) {
    this.restHighLevelClient = restHighLevelClient;
    this.objectMapper = objectMapper;
  }

  public boolean publishListing(Listing listing) {
    try {
      String listingJSON = objectMapper.writeValueAsString(listing);

      // Build Index Request
      IndexRequest indexRequest = Requests.indexRequest()
          .index(LISTING.getIndexName())
          .type(LISTING.getType())
          .id(listing.getId())
          .source(listingJSON, XContentType.JSON);

      return publishToES(indexRequest);
    } catch (JsonProcessingException e) {
      log.error(TAG, "publishListing: JSON Serialization Exception");
    }

    return false;
  }

  boolean publishToES(IndexRequest indexRequest) {
    Header header = new BasicHeader(HEADER_ACCEPT, HEADER_CONTENT_TYPE);
    try {
      // Index API to Publish to ElasticSearch
      IndexResponse response = restHighLevelClient.index(indexRequest, header);
      boolean result =
          response.status().equals(RestStatus.OK) || response.status().equals(RestStatus.CREATED);
      return result;
    } catch (IOException e) {
      log.error("publishToES; Error Publishing Document to ElasticSearch.  Document: {}",
          indexRequest.toString(), e);
    }
    return false;
  }

  public Listing getListing(String documentId) {
    log.info(TAG, "Retriving Listing: " + documentId);
    GetRequest getRequest = Requests
        .getRequest(LISTING.getIndexName())
        .id(documentId);

    try {
      GetResponse getResponse = restHighLevelClient.get(getRequest);
      if (getResponse.isExists()) {
        return objectMapper.convertValue(getResponse.getSource(), Listing.class);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  public List<Listing> getAllListings() {
    log.info(TAG, "Getting All Listings");

    // Search Source Builder
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    // Match All Query
    searchSourceBuilder.query(QueryBuilders.matchAllQuery());

    // Search Request
    SearchRequest searchRequest = new SearchRequest(LISTING.getIndexName());
    searchRequest.source(searchSourceBuilder);

    return executeSearch(searchRequest);
  }

  public List<Listing> getAllSaleListings(){
    log.info(TAG, "Getting All Sale Listings");

    // Search Source Builder
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    // Term Query
    BoolQueryBuilder boolQuery = new BoolQueryBuilder();
    boolQuery.must(QueryBuilders.termQuery(CATEGORY_KEY, "SALE"));

    searchSourceBuilder.query(boolQuery);

    // Search Request
    SearchRequest searchRequest = new SearchRequest(LISTING.getIndexName());
    searchRequest.source(searchSourceBuilder);

    return executeSearch(searchRequest);
  }

  public List<Listing> getAllHousingListingsInMinnesota(){
    log.info(TAG, "Getting All Sale Listings");

    // Search Source Builder
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    // Term Query
    BoolQueryBuilder boolQuery = new BoolQueryBuilder();
    boolQuery.must(QueryBuilders.termQuery(CATEGORY_KEY, HOUSING.toString()));
    boolQuery.must(QueryBuilders.termQuery(STATE_KEY, "MN"));

    searchSourceBuilder.query(boolQuery);

    // Search Request
    SearchRequest searchRequest = new SearchRequest(LISTING.getIndexName());
    searchRequest.source(searchSourceBuilder);

    return executeSearch(searchRequest);
  }

  public List<Listing> getAllListingsWithDescription(String description){
    log.info(TAG, "Getting Listings with Description");

    // Search Source Builder
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    // Batch Query (With Fuzziness)
    BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
    boolQueryBuilder.must( QueryBuilders.matchQuery(DESCRIPTION_KEY, description).fuzziness(Fuzziness.AUTO));

    searchSourceBuilder.query(boolQueryBuilder);

    // Search Request
    SearchRequest searchRequest = new SearchRequest(LISTING.getIndexName());
    searchRequest.source(searchSourceBuilder);

    return executeSearch(searchRequest);
  }






  private List<Listing> executeSearch(SearchRequest searchRequest){

    System.out.println(searchRequest.toString());

    try {
      SearchResponse response = restHighLevelClient.search(searchRequest);

      if (response.getHits().getTotalHits() > 0) {
        List<SearchHit> searchHits = Lists.newArrayList(response.getHits().getHits());

        Set<Map<String, Object>> resultJson = searchHits.stream()
            .map(SearchHit::getSourceAsMap)
            .collect(Collectors.toSet());

        List<Listing> result = objectMapper.convertValue(resultJson, new TypeReference<List<Listing>>() {});
        return result;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Lists.newArrayListWithExpectedSize(0);
  }


}

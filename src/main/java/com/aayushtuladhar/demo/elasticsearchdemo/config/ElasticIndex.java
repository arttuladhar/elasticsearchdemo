package com.aayushtuladhar.demo.elasticsearchdemo.config;

public enum ElasticIndex {
  LISTING("listing-index", "listing");

  private final String indexName;
  private final String type;

  ElasticIndex(String indexName, String type) {
    this.indexName = indexName;
    this.type = type;
  }

  public String getIndexName() {
    return indexName;
  }

  public String getType() {
    return type;
  }
}

package com.aayushtuladhar.demo.elasticsearchdemo.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.Charset;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  public static final Charset CHARSET_UTF8 = Charset.forName("UTF8");

  /**
   * Bean we'll use when talking to ElasticSearch.
   *
   * @param elasticUrl Where elastic search lives
   * @param elasticPort What port it's on
   * @param elasticHttpScheme http or https
   * @return Elastic Search's high level client
   */
  @Bean
  @Autowired
  public RestHighLevelClient restHighLevelClient(
      @Value("${elastic.url}") String elasticUrl,
      @Value("${elastic.port}") int elasticPort,
      @Value("${elastic.http.scheme}") String elasticHttpScheme) {

    return new RestHighLevelClient(RestClient.builder(
        new HttpHost(elasticUrl, elasticPort, elasticHttpScheme)));
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        false);
  }

}

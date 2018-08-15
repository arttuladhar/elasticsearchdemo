package com.aayushtuladhar.demo.elasticsearchdemo.domain;

import static com.aayushtuladhar.demo.elasticsearchdemo.config.AppConfig.CHARSET_UTF8;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.util.DigestUtils;

@Data
@JsonIgnoreProperties("id")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Listing {

  @JsonIgnore
  private String id;

  private String listingId;
  private String shortDescription;
  private String description;
  private Category category;
  private Double price;
  private String state;
  private String zipCode;
  private String createdDateEpocTS;
  private String expiryDateEpochTS;
  private String createdBy;

  public String getId() {
    return DigestUtils.md5DigestAsHex((getListingId() + getCreatedBy()).getBytes(CHARSET_UTF8));
  }
}

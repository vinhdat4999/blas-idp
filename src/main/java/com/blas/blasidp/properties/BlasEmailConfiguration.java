package com.blas.blasidp.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "blas.blas-email")
public class BlasEmailConfiguration {

  private String endpointHtmlEmail;
  private String endpointHtmlEmailWithAttachments;
}

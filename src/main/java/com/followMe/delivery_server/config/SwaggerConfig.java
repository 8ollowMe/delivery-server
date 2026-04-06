package com.followMe.delivery_server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI deliveryOpenAPI() {
    return new OpenAPI()
        .info(new Info().title("Delivery Server API").version("v1").description("배송 서버 API 문서"));
  }
}

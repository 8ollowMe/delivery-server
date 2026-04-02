package com.followMe.delivery_server.test;

import com.followMe.common.response.ApiResponse;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequestMapping("/internal/test")
@RequiredArgsConstructor
public class TestResetController {

  private final DataSource dataSource;

  @PostMapping("/reset")
  public ResponseEntity<ApiResponse> reset() throws Exception {
    try (var connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("mock-data.sql"));
    }
    return ApiResponse.ok();
  }
}

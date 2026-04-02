package com.followMe.delivery_server.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class AuditConfig {

  private static final UUID SYSTEM = UUID.fromString("00000000-0000-0000-0000-000000000000");

  @Bean
  public AuditorAware<UUID> auditorAware() {
    return () -> {
      var attrs = RequestContextHolder.getRequestAttributes();
      if (attrs instanceof ServletRequestAttributes servletAttrs) {
        HttpServletRequest request = servletAttrs.getRequest();
        String userId = request.getHeader("X-User-Id");
        if (userId != null) {
          try {
            return Optional.of(UUID.fromString(userId));
          } catch (IllegalArgumentException ignored) {
          }
        }
      }
      return Optional.of(SYSTEM);
    };
  }
}

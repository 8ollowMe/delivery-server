package com.followMe.delivery_server.delivery.presentation;

import com.followMe.common.response.ApiResponse;
import com.followMe.delivery_server.delivery.application.DeliveryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

  private final DeliveryService deliveryService;


  @GetMapping("/{deliveryId}")
  public ResponseEntity<ApiResponse> getDelivery(@PathVariable UUID deliveryId) {
    DeliveryResponseDto response = deliveryService.getDelivery(deliveryId);
    return ApiResponse.ok(response);
  }
}

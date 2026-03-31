package com.followMe.delivery_server.delivery.presentation;

import com.followMe.common.response.ApiResponse;
import com.followMe.delivery_server.delivery.application.DeliveryService;
import com.followMe.delivery_server.delivery.application.dto.DeliveryRequest;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponse;
import com.followMe.delivery_server.delivery.application.dto.StatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryInternalController {
  private final DeliveryService deliveryService;

  @PostMapping
  public ResponseEntity<ApiResponse> createDelivery(@RequestBody DeliveryRequest.Create command) {
    DeliveryResponse.DeliveryCreate response = deliveryService.createDelivery(command);
    return ApiResponse.ok(response);
}
  @GetMapping("/{deliveryId}/status")
  public ResponseEntity<ApiResponse> getDelivery(@PathVariable UUID deliveryId) {
    StatusResponse response = deliveryService.getDeliveryStatusForInternal(deliveryId);
    return ApiResponse.ok(response);
  }
}

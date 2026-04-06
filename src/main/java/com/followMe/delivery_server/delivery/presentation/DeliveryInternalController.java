package com.followMe.delivery_server.delivery.presentation;

import com.followMe.common.response.ApiResponse;
import com.followMe.delivery_server.delivery.application.DeliveryService;
import com.followMe.delivery_server.delivery.application.dto.DeliveryRequest;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponse;
import com.followMe.delivery_server.delivery.application.dto.StatusResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryInternalController {
  private final DeliveryService deliveryService;

  @PatchMapping("/{deliveryId}/cancel")
  public ResponseEntity<ApiResponse> cancelDelivery(@PathVariable UUID deliveryId) {
    deliveryService.cancelDeliveryForInternal(deliveryId);
    return ApiResponse.ok();
  }

  @PostMapping
  public DeliveryResponse.DeliveryCreate createDelivery(
      @RequestBody DeliveryRequest.Create command) {
    DeliveryResponse.DeliveryCreate response = deliveryService.createDelivery(command);
    return response;
  }

  @GetMapping("/{deliveryId}/status")
  public StatusResponse getDelivery(@PathVariable UUID deliveryId) {
    StatusResponse response = deliveryService.getDeliveryStatusForInternal(deliveryId);
    return response;
  }
}

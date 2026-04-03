package com.followMe.delivery_server.delivery.presentation;

import com.followMe.common.response.ApiResponse;
import com.followMe.delivery_server.delivery.application.DeliveryService;
import com.followMe.delivery_server.delivery.application.dto.DeliveryCreateResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryRequest;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryInternalController {
  private final DeliveryService deliveryService;

  @PostMapping
  public ResponseEntity<ApiResponse> createDelivery(@RequestBody DeliveryRequest.Create command) {
     DeliveryResponse.DeliveryCreate  response = deliveryService.createDelivery(command);
    return ApiResponse.ok(response);
  }
}

package com.followMe.delivery_server.delivery.presentation;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.common.response.ApiResponse;
import com.followMe.delivery_server.delivery.application.DeliveryService;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto;
import com.followMe.delivery_server.delivery.application.dto.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.application.dto.OrderCreateCommand;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

  private final DeliveryService deliveryService;

  @GetMapping
  public ResponseEntity<ApiResponse> getDeliveries(
      @ModelAttribute PageRequest pageRequest, @ModelAttribute DeliverySearchCondition condition) {
    PageResponse<DeliveryResponseDto> response =
        deliveryService.getDeliveries(pageRequest, condition);
    return ApiResponse.ok(response);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> createDelivery(@RequestBody OrderCreateCommand command) {
    deliveryService.createDelivery(command);
    return ApiResponse.ok();
  }

  @GetMapping("/{deliveryId}")
  public ResponseEntity<ApiResponse> getDelivery(@PathVariable UUID deliveryId) {
    DeliveryResponseDto response = deliveryService.getDelivery(deliveryId);
    return ApiResponse.ok(response);
  }
}

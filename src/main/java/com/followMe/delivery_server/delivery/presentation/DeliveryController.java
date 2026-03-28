package com.followMe.delivery_server.delivery.presentation;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.common.response.ApiResponse;
import com.followMe.delivery_server.delivery.application.DeliveryService;
import com.followMe.delivery_server.delivery.application.UserContext;
import com.followMe.delivery_server.delivery.application.UserRole;
import com.followMe.delivery_server.delivery.application.dto.DeliveryListItemDto;
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

  @ModelAttribute
  public UserContext userContext(
      @RequestHeader("X-User-Id") UUID userId,
      @RequestHeader("X-User-Role") UserRole role,
      @RequestHeader(value = "X-Hub-Id", required = false) UUID hubId,
      @RequestHeader(value = "X-Vendor-Id", required = false) UUID vendorId) {
    return new UserContext(userId, role, hubId, vendorId);
  }

  @GetMapping
  public ResponseEntity<ApiResponse> getDeliveries(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @ModelAttribute DeliverySearchCondition condition,
      @ModelAttribute UserContext user) {
    PageResponse<DeliveryListItemDto> response =
        deliveryService.getDeliveries(user, PageRequest.of(page, size), condition);
    return ApiResponse.ok(response);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> createDelivery(@RequestBody OrderCreateCommand command) {
    deliveryService.createDelivery(command);
    return ApiResponse.ok();
  }

  @GetMapping("/order/{orderId}")
  public ResponseEntity<ApiResponse> getDeliveryByOrderId(
      @PathVariable UUID orderId, @ModelAttribute UserContext user) {
    DeliveryResponseDto response = deliveryService.getDeliveryByOrderId(user, orderId);
    return ApiResponse.ok(response);
  }

  @GetMapping("/{deliveryId}")
  public ResponseEntity<ApiResponse> getDelivery(
      @PathVariable UUID deliveryId, @ModelAttribute UserContext user) {
    DeliveryResponseDto response = deliveryService.getDelivery(user, deliveryId);
    return ApiResponse.ok(response);
  }

  @PatchMapping("/{deliveryId}/cancel")
  public ResponseEntity<ApiResponse> cancelDelivery(
      @PathVariable UUID deliveryId, @ModelAttribute UserContext user) {
    deliveryService.cancelDelivery(user, deliveryId);
    return ApiResponse.ok();
  }
}

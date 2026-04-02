package com.followMe.delivery_server.delivery.presentation;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.common.response.ApiResponse;
import com.followMe.delivery_server.delivery.application.DeliveryService;
import com.followMe.delivery_server.delivery.application.ShipmentService;
import com.followMe.delivery_server.delivery.application.dto.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.domain.UserContext;
import com.followMe.delivery_server.delivery.domain.UserRole;
import com.followMe.delivery_server.delivery.presentation.dto.DeliveryRequest;
import com.followMe.delivery_server.delivery.presentation.dto.DeliveryResponse;
import com.followMe.delivery_server.delivery.presentation.dto.ShipmentResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

  private final DeliveryService deliveryService;
  private final ShipmentService shipmentService;

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
    PageResponse<DeliveryResponse.ListItem> response =
        deliveryService.getDeliveries(user, PageRequest.of(page, size), condition);
    return ApiResponse.ok(response);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> createDelivery(@RequestBody DeliveryRequest.Create request) {
    deliveryService.createDelivery(request);
    return ApiResponse.ok();
  }

  @GetMapping("/order/{orderId}")
  public ResponseEntity<ApiResponse> getDeliveryByOrderId(
      @PathVariable UUID orderId, @ModelAttribute UserContext user) {
    DeliveryResponse.Detail response = deliveryService.getDeliveryByOrderId(user, orderId);
    return ApiResponse.ok(response);
  }

  @GetMapping("/{deliveryId}")
  public ResponseEntity<ApiResponse> getDelivery(
      @PathVariable UUID deliveryId, @ModelAttribute UserContext user) {
    DeliveryResponse.Detail response = deliveryService.getDelivery(user, deliveryId);
    return ApiResponse.ok(response);
  }

  @PatchMapping("/{deliveryId}/cancel")
  public ResponseEntity<ApiResponse> cancelDelivery(
      @PathVariable UUID deliveryId, @ModelAttribute UserContext user) {
    deliveryService.cancelDelivery(user, deliveryId);
    return ApiResponse.ok();
  }

  @DeleteMapping("/{deliveryId}")
  public ResponseEntity<ApiResponse> deleteDelivery(
      @PathVariable UUID deliveryId, @ModelAttribute UserContext user) {
    deliveryService.deleteDelivery(user, deliveryId);
    return ApiResponse.ok();
  }

  @GetMapping("/{deliveryId}/shipments")
  public ResponseEntity<ApiResponse> getShipmentsByDeliveryId(
      @PathVariable UUID deliveryId, @ModelAttribute UserContext user) {
    List<ShipmentResponse.Detail> response =
        shipmentService.getShipmentsByDeliveryId(user, deliveryId);
    return ApiResponse.ok(response);
  }
}

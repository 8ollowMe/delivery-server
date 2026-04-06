package com.followMe.delivery_server.delivery.presentation;

import com.followMe.common.response.ApiResponse;
import com.followMe.delivery_server.delivery.application.ShipmentService;
import com.followMe.delivery_server.delivery.application.dto.ShipmentRequest;
import com.followMe.delivery_server.delivery.application.dto.ShipmentResponse;
import com.followMe.delivery_server.delivery.domain.UserContext;
import com.followMe.delivery_server.delivery.domain.UserRole;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

  private final ShipmentService shipmentService;

  @ModelAttribute
  public UserContext userContext(
      @RequestHeader("X-User-Id") UUID userId,
      @RequestHeader("X-User-Role") UserRole role,
      @RequestHeader(value = "X-Hub-Id", required = false) UUID hubId,
      @RequestHeader(value = "X-Vendor-Id", required = false) UUID vendorId) {
    return new UserContext(userId, role, hubId, vendorId);
  }

  @GetMapping("/{shipmentId}")
  public ResponseEntity<ApiResponse> getShipment(
      @PathVariable UUID shipmentId, @ModelAttribute UserContext user) {
    ShipmentResponse.Detail response = shipmentService.getShipment(user, shipmentId);
    return ApiResponse.ok(response);
  }

  @PatchMapping("/{shipmentId}/status")
  public ResponseEntity<ApiResponse> updateStatus(
      @PathVariable UUID shipmentId,
      @ModelAttribute UserContext user,
      @RequestBody @Valid ShipmentRequest.UpdateStatus request) {
    shipmentService.updateStatus(user, shipmentId, request.status());
    return ApiResponse.ok();
  }

  @PatchMapping("/{shipmentId}/reassign")
  public ResponseEntity<ApiResponse> reassignDeliveryManager(
      @PathVariable UUID shipmentId,
      @ModelAttribute UserContext user,
      @RequestBody @Valid ShipmentRequest.UpdateDeliveryManager request) {
    shipmentService.reassignDeliveryManager(user, shipmentId, request);
    return ApiResponse.ok();
  }
}

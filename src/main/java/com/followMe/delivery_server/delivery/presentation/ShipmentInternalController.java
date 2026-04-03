package com.followMe.delivery_server.delivery.presentation;

import com.followMe.common.response.ApiResponse;
import com.followMe.delivery_server.delivery.application.ShipmentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/v1/shipments")
@RequiredArgsConstructor
public class ShipmentInternalController {
  private final ShipmentService shipmentService;

}

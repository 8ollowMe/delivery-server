package com.followMe.delivery_server.delivery.presentation;

import com.followMe.delivery_server.delivery.application.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/v1/shipments")
@RequiredArgsConstructor
public class ShipmentInternalController {
  private final ShipmentService shipmentService;
}

package com.followMe.delivery_server.delivery.exception;

import com.followMe.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum DeliveryErrorCode implements ErrorCode {
  DELIVERY_NOT_FOUND("D001", "배달을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  DELIVERY_ALREADY_COMPLETED("D002", "이미 완료된 배달입니다.", HttpStatus.CONFLICT),
  INVALID_DELIVERY_STATUS("D003", "배달 상태가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

  SHIPMENT_NOT_FOUND("D004", "배송을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  SHIPMENT_ALREADY_COMPLETED("D005", "이미 완료된 배송입니다.", HttpStatus.CONFLICT),
  INVALID_SHIPMENT_STATUS("D006", "배송 상태가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
  NODE_TYPE_MISMATCH("D007", "노드 타입이 올바르지 않습니다.", HttpStatus.CONFLICT),
  INVALID_SHIPMENT_TYPE("D008", "배송 타입이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
  INVALID_NODE_INFORMATION("D009", "노드 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

  HUB_CLIENT_UNAVAILABLE("H001", "Hub 서비스를 일시적으로 사용할 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE),
  HUB_ROUTE_NOT_FOUND("H002", "Hub 경로 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  ;

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public HttpStatus getHttpStatus() {
    return httpStatus;
  }
}

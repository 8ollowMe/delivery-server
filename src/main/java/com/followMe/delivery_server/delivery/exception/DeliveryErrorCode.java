package com.followMe.delivery_server.delivery.exception;

import com.followMe.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum DeliveryErrorCode implements ErrorCode {
  DELIVERY_NOT_FOUND("D001", "배달을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  DELIVERY_ALREADY_COMPLETED("D002", "이미 완료된 배달입니다.", HttpStatus.CONFLICT),
  INVALID_DELIVERY_STATUS("D003", "배달 상태가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
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

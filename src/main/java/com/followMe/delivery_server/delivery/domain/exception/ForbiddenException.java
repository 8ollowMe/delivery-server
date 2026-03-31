package com.followMe.delivery_server.delivery.domain.exception;

public class ForbiddenExeption extends RuntimeException {
  public ForbiddenExeption(String message) {
    super(message);
  }
}

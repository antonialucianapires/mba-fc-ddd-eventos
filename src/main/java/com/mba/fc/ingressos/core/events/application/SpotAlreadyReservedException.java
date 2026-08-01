package com.mba.fc.ingressos.core.events.application;

public class SpotAlreadyReservedException extends RuntimeException {

  public SpotAlreadyReservedException(String message) {
    super(message);
  }

  public SpotAlreadyReservedException(String message, Throwable cause) {
    super(message, cause);
  }
}

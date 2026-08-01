package com.mba.fc.ingressos.core.events.application;

public class SpotNotAvailableException extends RuntimeException {

  public SpotNotAvailableException(String message) {
    super(message);
  }
}

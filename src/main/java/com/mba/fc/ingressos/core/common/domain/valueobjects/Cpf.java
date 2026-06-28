package com.mba.fc.ingressos.core.common.domain.valueobjects;

import com.mba.fc.ingressos.core.common.domain.ValueObject;

public class Cpf extends ValueObject<String> {

  public Cpf(String value) {
    super(value == null ? null : value.replaceAll("\\D", ""));
    this.validate();
  }

  private void validate() {
    if (value.length() != 11) {
      throw new IllegalArgumentException("CPF must have 11 digits, but has " + value.length());
    }

    boolean allDigitsEqual = value.matches("(\\d)\\1{10}");
    if (allDigitsEqual) {
      throw new IllegalArgumentException("CPF must have at least two different digits");
    }

    if (!isValidCheckDigits()) {
      throw new IllegalArgumentException("Invalid CPF: " + value);
    }
  }

  private boolean isValidCheckDigits() {
    // primeiro dígito verificador
    int sum = 0;
    for (int i = 0; i < 9; i++) {
      sum += Character.getNumericValue(value.charAt(i)) * (10 - i);
    }
    int firstDigit = (sum * 10) % 11;
    if (firstDigit == 10) firstDigit = 0;

    // segundo dígito verificador
    sum = 0;
    for (int i = 0; i < 10; i++) {
      sum += Character.getNumericValue(value.charAt(i)) * (11 - i);
    }
    int secondDigit = (sum * 10) % 11;
    if (secondDigit == 10) secondDigit = 0;

    return firstDigit == Character.getNumericValue(value.charAt(9))
        && secondDigit == Character.getNumericValue(value.charAt(10));
  }
}

package com.mba.fc.ingressos.core.common.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cpf")
class CpfTest {

    private static final String VALID_CPF = "52998224725";

    @Nested
    @DisplayName("Constructor")
    class Construction {

        @Test
        @DisplayName("should store CPF removing non-digit characters")
        void shouldRemoveNonDigitCharacters() {
            Cpf cpf = new Cpf("529.982.247-25");

            assertEquals(VALID_CPF, cpf.getValue());
        }

        @Test
        @DisplayName("should throw when CPF length is invalid")
        void shouldThrowForInvalidLength() {
            assertThrows(IllegalArgumentException.class, () -> new Cpf("123"));
        }

        @Test
        @DisplayName("should throw when CPF has all equal digits")
        void shouldThrowForAllEqualDigits() {
            assertThrows(IllegalArgumentException.class, () -> new Cpf("00000000000"));
        }

        @Test
        @DisplayName("should throw when CPF check digits are invalid")
        void shouldThrowForInvalidCheckDigits() {
            assertThrows(IllegalArgumentException.class, () -> new Cpf("11122233344"));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when both have the same CPF value")
        void shouldBeEqualWithSameValue() {
            Cpf a = new Cpf(VALID_CPF);
            Cpf b = new Cpf(VALID_CPF);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("should not be equal when values differ")
        void shouldNotBeEqualWithDifferentValues() {
            Cpf a = new Cpf(VALID_CPF);
            Cpf b = new Cpf("71428793860");

            assertNotEquals(a, b);
        }
    }
}
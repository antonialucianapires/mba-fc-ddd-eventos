package com.mba.fc.ingressos.core.common.domain;

import com.mba.fc.ingressos.core.common.domain.valueobjects.Cpf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValueObject")
class ValueObjectTest {

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when both have the same value and type")
        void shouldBeEqualWithSameValueAndType() {
            Cpf a = new Cpf("52998224725");
            Cpf b = new Cpf("52998224725");

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("should not be equal when values differ")
        void shouldNotBeEqualWithDifferentValues() {
            Cpf a = new Cpf("52998224725");
            Cpf b = new Cpf("71428793860");

            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            Cpf cpf = new Cpf("52998224725");

            assertEquals(cpf, cpf);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            Cpf cpf = new Cpf("52998224725");

            assertNotEquals(null, cpf);
        }
    }

    @Nested
    @DisplayName("Constructor")
    class Construction {

        @Test
        @DisplayName("should throw when value is null")
        void shouldThrowWhenValueIsNull() {
            assertThrows(IllegalArgumentException.class, () -> new Cpf(null));
        }
    }
}
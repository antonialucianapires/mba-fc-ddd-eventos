package com.mba.fc.ingressos.core.common.domain;

import com.mba.fc.ingressos.core.common.domain.valueobjects.CustomerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("Entity")
class EntityTest {

    private static class StubEntity extends Entity<CustomerId> {
        StubEntity(CustomerId id) {
            super(id);
        }

        @Override
        public String toString() {
            return "StubEntity{id=" + id.getValue() + "}";
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when both entities share the same ID")
        void shouldBeEqualWithSameId() {
            CustomerId id = new CustomerId();
            StubEntity a = new StubEntity(id);
            StubEntity b = new StubEntity(id);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("should not be equal when entities have different IDs")
        void shouldNotBeEqualWithDifferentIds() {
            StubEntity a = new StubEntity(new CustomerId());
            StubEntity b = new StubEntity(new CustomerId());

            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            StubEntity entity = new StubEntity(new CustomerId());

            assertEquals(entity, entity);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            StubEntity entity = new StubEntity(new CustomerId());

            assertNotEquals(null, entity);
        }
    }
}
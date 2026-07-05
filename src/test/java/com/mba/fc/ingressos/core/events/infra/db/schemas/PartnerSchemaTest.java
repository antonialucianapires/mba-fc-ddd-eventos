package com.mba.fc.ingressos.core.events.infra.db.schemas;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PartnerSchema")
class PartnerSchemaTest {

  private static final String VALID_ID = UUID.randomUUID().toString();
  private static final String VALID_NAME = "Acme Events";

  @Nested
  @DisplayName("Constructor")
  class Construction {

    @Test
    @DisplayName("no-arg constructor should create an instance without throwing")
    void shouldCreateWithNoArgConstructor() {
      assertDoesNotThrow((org.junit.jupiter.api.function.Executable) PartnerSchema::new);
    }

    @Test
    @DisplayName("no-arg constructor should leave id and name as null")
    void shouldHaveNullFieldsWithNoArgConstructor() {
      PartnerSchema schema = new PartnerSchema();

      assertNull(schema.getId());
      assertNull(schema.getName());
    }

    @Test
    @DisplayName("given a String id and name, should store both correctly")
    void shouldStoreStringIdAndName() {
      PartnerSchema schema = new PartnerSchema(VALID_ID, VALID_NAME);

      assertEquals(VALID_ID, schema.getId());
      assertEquals(VALID_NAME, schema.getName());
    }

    @Test
    @DisplayName("given a UUID and name, should convert UUID to String and store name")
    void shouldConvertUuidToStringAndStoreName() {
      UUID uuid = UUID.randomUUID();
      PartnerSchema schema = new PartnerSchema(uuid, VALID_NAME);

      assertEquals(uuid.toString(), schema.getId());
      assertEquals(VALID_NAME, schema.getName());
    }

    @Test
    @DisplayName("UUID constructor result should match String constructor for same value")
    void uuidAndStringConstructorsShouldProduceSameId() {
      UUID uuid = UUID.randomUUID();
      PartnerSchema fromUuid = new PartnerSchema(uuid, VALID_NAME);
      PartnerSchema fromString = new PartnerSchema(uuid.toString(), VALID_NAME);

      assertEquals(fromString.getId(), fromUuid.getId());
    }
  }

  @Nested
  @DisplayName("Getters")
  class Getters {

    @Test
    @DisplayName("getId should return the stored String id")
    void shouldReturnStoredId() {
      PartnerSchema schema = new PartnerSchema(VALID_ID, VALID_NAME);

      assertEquals(VALID_ID, schema.getId());
    }

    @Test
    @DisplayName("getName should return the stored name")
    void shouldReturnStoredName() {
      PartnerSchema schema = new PartnerSchema(VALID_ID, VALID_NAME);

      assertEquals(VALID_NAME, schema.getName());
    }
  }
}

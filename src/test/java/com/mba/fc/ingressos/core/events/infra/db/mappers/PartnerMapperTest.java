package com.mba.fc.ingressos.core.events.infra.db.mappers;

import static org.junit.jupiter.api.Assertions.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.domain.entities.Partner;
import com.mba.fc.ingressos.core.events.infra.db.schemas.PartnerSchema;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PartnerMapper")
class PartnerMapperTest {

  private static final String VALID_ID = UUID.randomUUID().toString();
  private static final String VALID_NAME = "Acme Events";

  private final PartnerMapper mapper = new PartnerMapper();

  @Nested
  @DisplayName("toDomain(PartnerSchema)")
  class ToDomain {

    @Test
    @DisplayName("should return a Partner with the same ID as the schema")
    void shouldMapIdFromSchema() {
      PartnerSchema schema = new PartnerSchema(VALID_ID, VALID_NAME);

      Partner partner = mapper.toDomain(schema);

      assertEquals(VALID_ID, partner.getId().getValue());
    }

    @Test
    @DisplayName("should wrap the schema ID into a PartnerId value object")
    void shouldWrapIdIntoPartnerId() {
      PartnerSchema schema = new PartnerSchema(VALID_ID, VALID_NAME);

      Partner partner = mapper.toDomain(schema);

      assertInstanceOf(PartnerId.class, partner.getId());
    }

    @Test
    @DisplayName("should return a Partner with the same name as the schema")
    void shouldMapNameFromSchema() {
      PartnerSchema schema = new PartnerSchema(VALID_ID, VALID_NAME);

      Partner partner = mapper.toDomain(schema);

      assertEquals(VALID_NAME, partner.getName());
    }

    @Test
    @DisplayName("each schema field should survive the mapping independently")
    void shouldMapAllFields() {
      String id = UUID.randomUUID().toString();
      String name = "Another Partner";
      PartnerSchema schema = new PartnerSchema(id, name);

      Partner partner = mapper.toDomain(schema);

      assertEquals(id, partner.getId().getValue());
      assertEquals(name, partner.getName());
    }
  }

  @Nested
  @DisplayName("toSchema(Partner)")
  class ToSchema {

    @Test
    @DisplayName("should return a PartnerSchema with the same ID as the partner")
    void shouldMapIdFromPartner() {
      Partner partner = new Partner(VALID_ID, VALID_NAME);

      PartnerSchema schema = mapper.toSchema(partner);

      assertEquals(VALID_ID, schema.getId());
    }

    @Test
    @DisplayName("should return a PartnerSchema with the same name as the partner")
    void shouldMapNameFromPartner() {
      Partner partner = new Partner(VALID_ID, VALID_NAME);

      PartnerSchema schema = mapper.toSchema(partner);

      assertEquals(VALID_NAME, schema.getName());
    }

    @Test
    @DisplayName("should store the partner ID as a plain String, not a UUID object")
    void shouldStoreIdAsString() {
      Partner partner = new Partner(VALID_ID, VALID_NAME);

      PartnerSchema schema = mapper.toSchema(partner);

      assertInstanceOf(String.class, schema.getId());
    }

    @Test
    @DisplayName("each partner field should survive the mapping independently")
    void shouldMapAllFields() {
      String id = UUID.randomUUID().toString();
      String name = "Another Partner";
      Partner partner = new Partner(id, name);

      PartnerSchema schema = mapper.toSchema(partner);

      assertEquals(id, schema.getId());
      assertEquals(name, schema.getName());
    }
  }

  @Nested
  @DisplayName("Round-trip")
  class RoundTrip {

    @Test
    @DisplayName("toDomain(toSchema(partner)) should preserve the original ID")
    void domainToSchemaAndBackShouldPreserveId() {
      Partner original = new Partner(VALID_ID, VALID_NAME);

      Partner roundTripped = mapper.toDomain(mapper.toSchema(original));

      assertEquals(original.getId().getValue(), roundTripped.getId().getValue());
    }

    @Test
    @DisplayName("toDomain(toSchema(partner)) should preserve the original name")
    void domainToSchemaAndBackShouldPreserveName() {
      Partner original = new Partner(VALID_ID, VALID_NAME);

      Partner roundTripped = mapper.toDomain(mapper.toSchema(original));

      assertEquals(original.getName(), roundTripped.getName());
    }

    @Test
    @DisplayName("toSchema(toDomain(schema)) should preserve the original ID")
    void schemaToDomainAndBackShouldPreserveId() {
      PartnerSchema original = new PartnerSchema(VALID_ID, VALID_NAME);

      PartnerSchema roundTripped = mapper.toSchema(mapper.toDomain(original));

      assertEquals(original.getId(), roundTripped.getId());
    }

    @Test
    @DisplayName("toSchema(toDomain(schema)) should preserve the original name")
    void schemaToDomainAndBackShouldPreserveName() {
      PartnerSchema original = new PartnerSchema(VALID_ID, VALID_NAME);

      PartnerSchema roundTripped = mapper.toSchema(mapper.toDomain(original));

      assertEquals(original.getName(), roundTripped.getName());
    }
  }
}

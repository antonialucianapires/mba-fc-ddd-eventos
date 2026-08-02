package com.mba.fc.ingressos.events.partners;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mba.fc.ingressos.core.events.application.PartnerService;
import com.mba.fc.ingressos.core.events.domain.entities.Partner;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PartnersController.class)
@DisplayName("PartnersController")
class PartnersControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PartnerService partnerService;

  @Test
  @DisplayName("GET /partners should return every partner")
  void shouldListPartners() throws Exception {
    Partner partner = Partner.create("Acme Corp");
    when(partnerService.list()).thenReturn(Set.of(partner));

    mockMvc
        .perform(get("/partners"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(partner.getId().getValue()))
        .andExpect(jsonPath("$[0].name").value("Acme Corp"));
  }

  @Test
  @DisplayName("POST /partners should create a partner")
  void shouldCreatePartner() throws Exception {
    Partner partner = Partner.create("Acme Corp");
    when(partnerService.create("Acme Corp")).thenReturn(partner);

    mockMvc
        .perform(
            post("/partners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Acme Corp\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(partner.getId().getValue()))
        .andExpect(jsonPath("$.name").value("Acme Corp"));

    verify(partnerService).create("Acme Corp");
  }
}

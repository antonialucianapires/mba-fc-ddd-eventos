package com.mba.fc.ingressos.events.eventsections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.application.EventService;
import com.mba.fc.ingressos.core.events.domain.commands.AddSectionCommand;
import com.mba.fc.ingressos.core.events.domain.commands.CreateEventCommand;
import com.mba.fc.ingressos.core.events.domain.commands.UpdateEventSectionCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Event;
import com.mba.fc.ingressos.core.events.domain.entities.EventSection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventSectionsController.class)
@DisplayName("EventSectionsController")
class EventSectionsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private EventService eventService;

  private Event newEvent() {
    return Event.create(
        new CreateEventCommand(
            "Show de Rock", "Um grande show", LocalDate.of(2026, 12, 31), 0, new PartnerId()));
  }

  @Test
  @DisplayName("GET /events/{eventId}/sections should return every section")
  void shouldListSections() throws Exception {
    Event event = newEvent();
    event.addSection(new AddSectionCommand("Pista", "Seção pista", 3, new BigDecimal("50.00")));
    EventSection section = event.getSections().iterator().next();
    String eventId = event.getId().getValue();
    when(eventService.listSections(new EventId(eventId))).thenReturn(Set.of(section));

    mockMvc
        .perform(get("/events/{eventId}/sections", eventId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(section.getId().getValue()))
        .andExpect(jsonPath("$[0].name").value("Pista"))
        .andExpect(jsonPath("$[0].spots.length()").value(3));
  }

  @Test
  @DisplayName("POST /events/{eventId}/sections should add a section and return the updated event")
  void shouldAddSection() throws Exception {
    Event event = newEvent();
    event.addSection(new AddSectionCommand("Pista", "Seção pista", 3, new BigDecimal("50.00")));
    String eventId = event.getId().getValue();
    when(eventService.addSection(eq(new EventId(eventId)), any(AddSectionCommand.class)))
        .thenReturn(event);

    mockMvc
        .perform(
            post("/events/{eventId}/sections", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Pista\",\"description\":\"Seção pista\",\"totalSpots\":3,\"price\":50.00}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(eventId))
        .andExpect(jsonPath("$.sections[0].name").value("Pista"));
  }

  @Test
  @DisplayName("PUT /events/{eventId}/sections/{sectionId} should update the section")
  void shouldUpdateSection() throws Exception {
    Event event = newEvent();
    event.addSection(new AddSectionCommand("Pista", "Seção pista", 3, new BigDecimal("50.00")));
    String eventId = event.getId().getValue();
    String sectionId = event.getSections().iterator().next().getId().getValue();
    when(eventService.updateSection(
            eq(new EventId(eventId)), eq(new EventSectionId(sectionId)), any(UpdateEventSectionCommand.class)))
        .thenReturn(event);

    mockMvc
        .perform(
            put("/events/{eventId}/sections/{sectionId}", eventId, sectionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"price\":75.00}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(eventId));
  }
}

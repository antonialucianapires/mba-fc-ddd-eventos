package com.mba.fc.ingressos.events.eventspots;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.application.EventService;
import com.mba.fc.ingressos.core.events.domain.commands.AddSectionCommand;
import com.mba.fc.ingressos.core.events.domain.commands.CreateEventCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Event;
import com.mba.fc.ingressos.core.events.domain.entities.EventSection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventSpotsController.class)
@DisplayName("EventSpotsController")
class EventSpotsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private EventService eventService;

  private Event newEventWithSection() {
    Event event =
        Event.create(
            new CreateEventCommand(
                "Show de Rock", "Um grande show", LocalDate.of(2026, 12, 31), 0, new PartnerId()));
    event.addSection(new AddSectionCommand("Pista", "Seção pista", 3, new BigDecimal("50.00")));
    return event;
  }

  @Test
  @DisplayName("GET .../spots should return every spot from the section")
  void shouldListSpots() throws Exception {
    Event event = newEventWithSection();
    EventSection section = event.getSections().iterator().next();
    String eventId = event.getId().getValue();
    String sectionId = section.getId().getValue();
    when(eventService.listSections(new EventId(eventId))).thenReturn(Set.of(section));

    mockMvc
        .perform(get("/events/{eventId}/sections/{sectionId}/spots", eventId, sectionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3));
  }

  @Test
  @DisplayName("GET .../spots should fail when the section does not belong to the event")
  void shouldFailWhenSectionNotFound() {
    Event event = newEventWithSection();
    String eventId = event.getId().getValue();
    when(eventService.listSections(new EventId(eventId))).thenReturn(Set.of());

    Exception exception =
        assertThrows(
            Exception.class,
            () ->
                mockMvc.perform(
                    get(
                        "/events/{eventId}/sections/{sectionId}/spots",
                        eventId,
                        new PartnerId().getValue())));

    assertTrue(exception.getCause() instanceof IllegalArgumentException);
  }
}

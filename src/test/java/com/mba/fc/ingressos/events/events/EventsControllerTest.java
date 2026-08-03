package com.mba.fc.ingressos.events.events;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.application.EventService;
import com.mba.fc.ingressos.core.events.domain.commands.CreateEventCommand;
import com.mba.fc.ingressos.core.events.domain.commands.UpdateEventCommand;
import com.mba.fc.ingressos.core.events.domain.entities.Event;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventsController.class)
@DisplayName("EventsController")
class EventsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private EventService eventService;

  private Event newEvent(PartnerId partnerId) {
    return Event.create(
        new CreateEventCommand(
            "Show de Rock", "Um grande show", LocalDate.of(2026, 12, 31), 100, partnerId));
  }

  @Test
  @DisplayName("GET /events should return every event")
  void shouldListEvents() throws Exception {
    Event event = newEvent(new PartnerId());
    when(eventService.list()).thenReturn(Set.of(event));

    mockMvc
        .perform(get("/events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(event.getId().getValue()))
        .andExpect(jsonPath("$[0].name").value("Show de Rock"));
  }

  @Test
  @DisplayName("POST /events should create an event")
  void shouldCreateEvent() throws Exception {
    PartnerId partnerId = new PartnerId();
    Event event = newEvent(partnerId);
    when(eventService.create(any(CreateEventCommand.class))).thenReturn(event);

    mockMvc
        .perform(
            post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Show de Rock\",\"description\":\"Um grande show\","
                        + "\"date\":\"2026-12-31\",\"totalSpots\":100,\"partnerId\":\""
                        + partnerId.getValue()
                        + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Show de Rock"))
        .andExpect(jsonPath("$.date").value("2026-12-31"));
  }

  @Test
  @DisplayName("PUT /events/{id} should update the event")
  void shouldUpdateEvent() throws Exception {
    Event event = newEvent(new PartnerId()).changeName("Show Atualizado");
    String id = event.getId().getValue();
    when(eventService.update(eq(new EventId(id)), any(UpdateEventCommand.class))).thenReturn(event);

    mockMvc
        .perform(
            put("/events/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Show Atualizado\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Show Atualizado"));
  }

  @Test
  @DisplayName("DELETE /events/{id} should return 204")
  void shouldDeleteEvent() throws Exception {
    String id = new EventId().getValue();

    mockMvc.perform(delete("/events/{id}", id)).andExpect(status().isNoContent());

    verify(eventService).delete(new EventId(id));
  }
}

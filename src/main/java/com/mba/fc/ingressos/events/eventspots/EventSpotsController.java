package com.mba.fc.ingressos.events.eventspots;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.events.application.EventService;
import com.mba.fc.ingressos.core.events.domain.entities.EventSection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events/{eventId}/sections/{sectionId}/spots")
public class EventSpotsController {

  private final EventService eventService;

  public EventSpotsController(EventService eventService) {
    this.eventService = eventService;
  }

  @GetMapping
  public Set<EventSpotResponse> getSpots(
      @PathVariable String eventId, @PathVariable String sectionId) {
    EventSectionId id = new EventSectionId(sectionId);
    EventSection section =
        eventService.listSections(new EventId(eventId)).stream()
            .filter(candidate -> candidate.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Section not found"));
    return section.getSpots().stream().map(EventSpotResponse::new).collect(Collectors.toSet());
  }
}

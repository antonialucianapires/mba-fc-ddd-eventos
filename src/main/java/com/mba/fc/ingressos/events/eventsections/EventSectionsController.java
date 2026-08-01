package com.mba.fc.ingressos.events.eventsections;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.EventSectionId;
import com.mba.fc.ingressos.core.events.application.EventService;
import com.mba.fc.ingressos.core.events.domain.commands.AddSectionCommand;
import com.mba.fc.ingressos.core.events.domain.commands.UpdateEventSectionCommand;
import com.mba.fc.ingressos.events.events.EventResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events/{eventId}/sections")
public class EventSectionsController {

    private final EventService eventService;

    public EventSectionsController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public Set<EventSectionResponse> getSections(@PathVariable String eventId) {
        return eventService.listSections(new EventId(eventId)).stream()
                .map(EventSectionResponse::new)
                .collect(Collectors.toSet());
    }

    @PostMapping
    public EventResponse addSection(@PathVariable String eventId, @RequestBody AddSectionRequest request) {
        AddSectionCommand command = new AddSectionCommand(
                request.name(), request.description(), request.totalSpots(), request.price());
        return new EventResponse(eventService.addSection(new EventId(eventId), command));
    }

    @PutMapping("/{sectionId}")
    public EventResponse updateSection(
            @PathVariable String eventId,
            @PathVariable String sectionId,
            @RequestBody UpdateEventSectionRequest request) {
        UpdateEventSectionCommand command = new UpdateEventSectionCommand(
                Optional.ofNullable(request.name()),
                Optional.ofNullable(request.description()),
                Optional.ofNullable(request.price()));
        return new EventResponse(
                eventService.updateSection(new EventId(eventId), new EventSectionId(sectionId), command));
    }
}

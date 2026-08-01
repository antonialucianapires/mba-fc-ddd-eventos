package com.mba.fc.ingressos.events.events;

import com.mba.fc.ingressos.core.common.domain.valueobjects.EventId;
import com.mba.fc.ingressos.core.common.domain.valueobjects.PartnerId;
import com.mba.fc.ingressos.core.events.application.EventService;
import com.mba.fc.ingressos.core.events.domain.commands.CreateEventCommand;
import com.mba.fc.ingressos.core.events.domain.commands.UpdateEventCommand;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events")
public class EventsController {

    private final EventService eventService;

    public EventsController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public Set<EventResponse> getEvents() {
        return eventService.list().stream().map(EventResponse::new).collect(Collectors.toSet());
    }

    @PostMapping
    public EventResponse createEvent(@RequestBody EventRequest request) {
        CreateEventCommand command = new CreateEventCommand(
                request.name(),
                request.description(),
                request.date(),
                request.totalSpots(),
                new PartnerId(request.partnerId()));
        return new EventResponse(eventService.create(command));
    }

    @PutMapping("/{id}")
    public EventResponse updateEvent(@PathVariable String id, @RequestBody UpdateEventRequest request) {
        UpdateEventCommand command = new UpdateEventCommand(
                Optional.ofNullable(request.name()),
                Optional.ofNullable(request.description()),
                Optional.ofNullable(request.date()));
        return new EventResponse(eventService.update(new EventId(id), command));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable String id) {
        eventService.delete(new EventId(id));
    }
}

package com.example.eventpro.service;

import com.example.eventpro.entity.Event;
import com.example.eventpro.entity.Organizer;
import com.example.eventpro.enums.EventStatus;
import com.example.eventpro.repository.EventRepository;
import com.example.eventpro.repository.OrganizerRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;

    public EventService(
            EventRepository eventRepository,
            OrganizerRepository organizerRepository) {
        this.eventRepository = eventRepository;
        this.organizerRepository = organizerRepository;
    }

    public Event createEvent(Event event, Long organizerId) {

        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Organisateur introuvable : " + organizerId));

        event.setId(null);
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.DRAFT);

        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Événement introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<Event> getPublishedEvents() {
        return eventRepository.findByStatus(
                EventStatus.PUBLISHED);
    }

    public Event updateEvent(Long id, Event updatedEvent) {

        Event event = getEventById(id);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new IllegalStateException(
                    "Seuls les événements en brouillon sont modifiables.");
        }

        event.setTitle(updatedEvent.getTitle());
        event.setDescription(updatedEvent.getDescription());
        event.setLocation(updatedEvent.getLocation());
        event.setStartDate(updatedEvent.getStartDate());
        event.setEndDate(updatedEvent.getEndDate());
        event.setSalesOpeningDate(
                updatedEvent.getSalesOpeningDate());
        event.setSalesClosingDate(
                updatedEvent.getSalesClosingDate());
        event.setCapacity(updatedEvent.getCapacity());

        return eventRepository.save(event);
    }

    public Event publishEvent(Long id) {

        Event event = getEventById(id);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new IllegalStateException(
                    "Seul un événement en brouillon peut être publié.");
        }

        event.publish();

        return eventRepository.save(event);
    }

    public Event cancelEvent(Long id) {

        Event event = getEventById(id);

        if (event.getStatus() == EventStatus.COMPLETED
                || event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cet événement ne peut pas être annulé.");
        }

        event.cancel();

        return eventRepository.save(event);
    }
}
package za.co.hydra24.entry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import za.co.hydra24.api.ApiErrors;
import za.co.hydra24.classes.ClassRepository;
import za.co.hydra24.classes.PricedClass;
import za.co.hydra24.event.Event;
import za.co.hydra24.event.EventRepository;
import za.co.hydra24.profile.ProfileRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** The rules for entering and withdrawing. No payment is taken: the cost is only recorded. */
@ApplicationScoped
public class EntryService {

    @Inject
    EntryRepository entries;

    @Inject
    EventRepository events;

    @Inject
    ClassRepository classes;

    @Inject
    ProfileRepository profiles;

    public Optional<Entry> find(UUID cognitoSub, long eventId) {
        return profiles.findId(cognitoSub).flatMap(profileId -> entries.find(eventId, profileId));
    }

    public List<MyEntry> listMine(UUID cognitoSub) {
        return profiles.findId(cognitoSub).map(entries::listForProfile).orElse(List.of());
    }

    public void enter(UUID cognitoSub, long eventId, EntryForm form) {
        Event event = openEvent(eventId);
        long profileId = profiles.findId(cognitoSub)
                .orElseThrow(() -> ApiErrors.conflict("Complete your profile before entering an event."));
        if (!event.offersAttendance(form.attendance())) {
            throw ApiErrors.badRequest("Choose which days you will attend.");
        }
        PricedClass chosen = classes.listEnabledFor(event.eventType()).stream()
                .filter(c -> c.id() == form.classId())
                .findFirst()
                .orElseThrow(() -> ApiErrors.badRequest("That class is not available for this event."));
        entries.save(eventId, profileId, chosen.id(), form.attendance(), chosen.cost());
    }

    public void withdraw(UUID cognitoSub, long eventId) {
        openEvent(eventId);
        profiles.findId(cognitoSub).ifPresent(profileId -> entries.delete(eventId, profileId));
    }

    private Event openEvent(long eventId) {
        Event event = events.find(eventId).orElseThrow(NotFoundException::new);
        if (!event.entriesOpen()) {
            throw ApiErrors.conflict("Entries for this event have closed.");
        }
        return event;
    }
}

import java.util.ArrayList;
import java.util.List;

/** In-memory fallback used when no database is reachable. */
public class MemoryStore implements Store {
    private final List<Venue> venues = new ArrayList<>();
    private final List<Event> events = new ArrayList<>();
    private final List<Participant> participants = new ArrayList<>();
    private int venueSeq = 1, eventSeq = 1, partSeq = 1;

    @Override public String getModeName() { return "In-memory (no database)"; }

    @Override public synchronized Venue addVenue(String name, int capacity) {
        Venue v = new Venue(venueSeq++, name, capacity);
        venues.add(v);
        return v;
    }

    @Override public synchronized List<Venue> getVenues() { return new ArrayList<>(venues); }

    @Override public synchronized Event addEvent(Event e) {
        e.setId(eventSeq++);
        events.add(e);
        return e;
    }

    @Override public synchronized List<Event> getEvents() { return new ArrayList<>(events); }

    @Override public synchronized void setVenue(Event e, Venue v) { e.setVenue(v); }

    @Override public synchronized Participant addParticipant(Participant p) {
        p.setId(partSeq++);
        participants.add(p);
        return p;
    }

    @Override public synchronized List<Participant> getParticipants(int eventId) {
        List<Participant> out = new ArrayList<>();
        for (Participant p : participants) if (p.getEventId() == eventId) out.add(p);
        return out;
    }
}

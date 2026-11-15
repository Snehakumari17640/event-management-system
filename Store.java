import java.sql.SQLException;
import java.util.List;

/** Data access layer. JdbcStore (MySQL etc.) and MemoryStore (fallback) implement it. */
public interface Store {
    String getModeName();
    Venue addVenue(String name, int capacity) throws SQLException;
    List<Venue> getVenues() throws SQLException;
    Event addEvent(Event e) throws SQLException;
    List<Event> getEvents() throws SQLException;
    void setVenue(Event e, Venue v) throws SQLException;
    Participant addParticipant(Participant p) throws SQLException;
    List<Participant> getParticipants(int eventId) throws SQLException;

    /** Unit 3: allot a venue, throwing VenueConflictException on clash or small capacity. */
    default void allotVenue(Event e, Venue v) throws VenueConflictException, SQLException {
        if (v.getCapacity() < e.getCapacity()) {
            throw new VenueConflictException("Venue '" + v.getName() + "' holds " + v.getCapacity()
                    + " but the event needs " + e.getCapacity() + ".");
        }
        for (Event other : getEvents()) {
            if (other.getId() != e.getId() && other.getVenue() != null
                    && other.getVenue().getId() == v.getId() && e.overlaps(other)) {
                throw new VenueConflictException("Venue '" + v.getName() + "' is already booked for '"
                        + other.getName() + "' on " + other.getDate() + " ("
                        + other.getStartTime() + "-" + other.getEndTime() + ").");
            }
        }
        setVenue(e, v);
    }
}

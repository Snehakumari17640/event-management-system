import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Unit 1 + 2: abstract base class of Seminar, Workshop and Fest; has inner class Session. */
public abstract class Event implements Schedulable {
    private int id;
    private final String name;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int capacity;
    private Venue venue;
    private final List<Session> sessions = new ArrayList<>();

    protected Event(int id, String name, LocalDate date, LocalTime start, LocalTime end, int capacity) {
        this.id = id;
        this.name = name.trim();
        this.date = date;
        this.startTime = start;
        this.endTime = end;
        this.capacity = capacity;
    }

    /** Factory used by the GUI and the database layer. */
    public static Event create(String type, int id, String name, LocalDate d,
                               LocalTime s, LocalTime e, int capacity) {
        switch (type) {
            case "Seminar":  return new Seminar(id, name, d, s, e, capacity);
            case "Workshop": return new Workshop(id, name, d, s, e, capacity);
            case "Fest":     return new Fest(id, name, d, s, e, capacity);
            default: throw new IllegalArgumentException("Unknown event type: " + type);
        }
    }

    public abstract String getType();
    public abstract String describe();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    @Override public LocalDate getDate() { return date; }
    @Override public LocalTime getStartTime() { return startTime; }
    @Override public LocalTime getEndTime() { return endTime; }
    @Override public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }

    public Session addSession(String title, LocalTime time) {
        if (time.isBefore(startTime) || time.isAfter(endTime)) {
            throw new IllegalArgumentException("Session must be within " + startTime + " - " + endTime);
        }
        Session s = new Session(title, time);
        sessions.add(s);
        return s;
    }

    public List<Session> getSessions() {
        return Collections.unmodifiableList(sessions);
    }

    @Override
    public String toString() {
        return "#" + id + " " + name + " [" + getType() + "]";
    }

    /** Unit 2: inner class Session - a timed slot inside an event. */
    public class Session {
        private final String title;
        private final LocalTime time;

        private Session(String title, LocalTime time) {
            this.title = title.trim();
            this.time = time;
        }

        public String getTitle() { return title; }
        public LocalTime getTime() { return time; }

        @Override
        public String toString() {
            return time + " " + title + " (" + Event.this.name + ")";
        }
    }
}

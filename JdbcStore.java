import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/** Unit 4: JDBC storage using PreparedStatement for every query on events and participants. */
public class JdbcStore implements Store {
    private final Connection con;
    private final String url;
    private final Map<Integer, Event> cache = new LinkedHashMap<>(); // keeps in-memory sessions

    public JdbcStore(String url, String user, String password) throws SQLException {
        this.url = url;
        this.con = DriverManager.getConnection(url, user, password);
        try (Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS venues ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, capacity INT NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS events ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(150) NOT NULL, type VARCHAR(20) NOT NULL,"
                    + "event_date VARCHAR(10) NOT NULL, start_time VARCHAR(5) NOT NULL, end_time VARCHAR(5) NOT NULL,"
                    + "capacity INT NOT NULL, venue_id INT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS participants ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, event_id INT NOT NULL, name VARCHAR(100) NOT NULL,"
                    + "email VARCHAR(150) NOT NULL, phone VARCHAR(15) NOT NULL)");
        }
    }

    @Override public String getModeName() { return "JDBC: " + url; }

    @Override
    public synchronized Venue addVenue(String name, int capacity) throws SQLException {
        String sql = "INSERT INTO venues(name, capacity) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, capacity);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                return new Venue(k.getInt(1), name, capacity);
            }
        }
    }

    @Override
    public synchronized List<Venue> getVenues() throws SQLException {
        List<Venue> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement("SELECT id, name, capacity FROM venues ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Venue(rs.getInt(1), rs.getString(2), rs.getInt(3)));
        }
        return list;
    }

    @Override
    public synchronized Event addEvent(Event e) throws SQLException {
        String sql = "INSERT INTO events(name, type, event_date, start_time, end_time, capacity) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getType());
            ps.setString(3, e.getDate().toString());
            ps.setString(4, e.getStartTime().toString());
            ps.setString(5, e.getEndTime().toString());
            ps.setInt(6, e.getCapacity());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                e.setId(k.getInt(1));
            }
        }
        cache.put(e.getId(), e);
        return e;
    }

    @Override
    public synchronized List<Event> getEvents() throws SQLException {
        Map<Integer, Venue> venueById = new HashMap<>();
        for (Venue v : getVenues()) venueById.put(v.getId(), v);
        List<Event> list = new ArrayList<>();
        String sql = "SELECT id, name, type, event_date, start_time, end_time, capacity, venue_id FROM events ORDER BY id";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt(1);
                Event ev = cache.get(id);
                if (ev == null) {
                    ev = Event.create(rs.getString(3), id, rs.getString(2), LocalDate.parse(rs.getString(4)),
                            LocalTime.parse(rs.getString(5)), LocalTime.parse(rs.getString(6)), rs.getInt(7));
                    cache.put(id, ev);
                }
                int vid = rs.getInt(8);
                ev.setVenue(rs.wasNull() ? null : venueById.get(vid));
                list.add(ev);
            }
        }
        return list;
    }

    @Override
    public synchronized void setVenue(Event e, Venue v) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE events SET venue_id = ? WHERE id = ?")) {
            ps.setInt(1, v.getId());
            ps.setInt(2, e.getId());
            ps.executeUpdate();
        }
        e.setVenue(v);
    }

    @Override
    public synchronized Participant addParticipant(Participant p) throws SQLException {
        String sql = "INSERT INTO participants(event_id, name, email, phone) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getEventId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getPhone());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                p.setId(k.getInt(1));
            }
        }
        return p;
    }

    @Override
    public synchronized List<Participant> getParticipants(int eventId) throws SQLException {
        List<Participant> list = new ArrayList<>();
        String sql = "SELECT id, event_id, name, email, phone FROM participants WHERE event_id = ? ORDER BY name";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Participant(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5)));
                }
            }
        }
        return list;
    }
}

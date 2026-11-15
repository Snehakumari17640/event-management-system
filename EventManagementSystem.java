import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/**
 * Event Management System - Swing GUI (Unit 5: FlowLayout, BorderLayout, ActionListener).
 * Tabs: Events | Registration | Venues | Schedule | Participants
 */
public class EventManagementSystem extends JFrame implements ActionListener {

    private final Store store;
    private boolean loading = false;

    private final JLabel status = new JLabel(" ");

    // Module 1: Event creation
    private final JTextField evName = new JTextField(14);
    private final JTextField evDate = new JTextField(LocalDate.now().toString(), 8);
    private final JTextField evStart = new JTextField("10:00", 5);
    private final JTextField evEnd = new JTextField("12:00", 5);
    private final JTextField evCap = new JTextField("50", 4);
    private final JComboBox<String> evType = new JComboBox<>(new String[]{"Seminar", "Workshop", "Fest"});
    private final JButton btnAddEvent = new JButton("Create Event");
    private final DefaultTableModel eventModel = model("ID", "Name", "Type", "Date", "Start", "End", "Capacity", "Venue");

    // Module 2: Participant registration
    private final JComboBox<Event> regEvent = new JComboBox<>();
    private final JTextField regName = new JTextField(12);
    private final JTextField regEmail = new JTextField(16);
    private final JTextField regPhone = new JTextField(10);
    private final JButton btnRegister = new JButton("Register");

    // Module 3: Venue allotment
    private final JTextField venName = new JTextField(14);
    private final JTextField venCap = new JTextField("100", 5);
    private final JButton btnAddVenue = new JButton("Add Venue");
    private final JComboBox<Event> allotEvent = new JComboBox<>();
    private final JComboBox<Venue> allotVenue = new JComboBox<>();
    private final JButton btnAllot = new JButton("Allot Venue");
    private final DefaultTableModel venueModel = model("ID", "Venue", "Capacity");

    // Module 4: Schedule view
    private final DefaultTableModel scheduleModel = model("Date", "Time", "Event", "Type", "Venue", "Sessions");
    private final JComboBox<Event> sessEvent = new JComboBox<>();
    private final JTextField sessTitle = new JTextField(14);
    private final JTextField sessTime = new JTextField("10:30", 5);
    private final JButton btnAddSession = new JButton("Add Session");

    // Module 5: Participant list
    private final JComboBox<Event> listEvent = new JComboBox<>();
    private final DefaultTableModel partModel = model("ID", "Name", "Email", "Phone");
    private final JButton btnExport = new JButton("Export to CSV...");

    public EventManagementSystem(Store store) {
        super("Event Management System");
        this.store = store;

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Events", buildEventsTab());
        tabs.addTab("Registration", buildRegistrationTab());
        tabs.addTab("Venues", buildVenuesTab());
        tabs.addTab("Schedule", buildScheduleTab());
        tabs.addTab("Participants", buildParticipantsTab());

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        bottom.add(status, BorderLayout.CENTER);
        bottom.add(new JLabel("Storage: " + store.getModeName()), BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // wire listeners
        for (JButton b : new JButton[]{btnAddEvent, btnRegister, btnAddVenue, btnAllot, btnAddSession, btnExport}) {
            b.addActionListener(this);
        }
        listEvent.addActionListener(this);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 560);
        setLocationRelativeTo(null);

        refreshAll();

        // Unit 3: reminder thread, polls every 30s, reminds 15 minutes ahead
        ReminderThread reminder = new ReminderThread(store, 15, 30_000, msg ->
                SwingUtilities.invokeLater(() -> {
                    status.setText(msg);
                    JOptionPane.showMessageDialog(this, msg, "Event Reminder", JOptionPane.INFORMATION_MESSAGE);
                }));
        reminder.start();
    }

    // ---------- tab builders ----------

    private JPanel buildEventsTab() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        form.add(new JLabel("Name:")); form.add(evName);
        form.add(new JLabel("Type:")); form.add(evType);
        form.add(new JLabel("Date (yyyy-mm-dd):")); form.add(evDate);
        form.add(new JLabel("Start (HH:mm):")); form.add(evStart);
        form.add(new JLabel("End:")); form.add(evEnd);
        form.add(new JLabel("Capacity:")); form.add(evCap);
        form.add(btnAddEvent);
        return tabPanel(form, eventModel, null);
    }

    private JPanel buildRegistrationTab() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        form.add(new JLabel("Event:")); form.add(regEvent);
        form.add(new JLabel("Name:")); form.add(regName);
        form.add(new JLabel("Email:")); form.add(regEmail);
        form.add(new JLabel("Phone (10 digits):")); form.add(regPhone);
        form.add(btnRegister);
        JPanel p = new JPanel(new BorderLayout());
        p.add(form, BorderLayout.NORTH);
        JTextArea help = new JTextArea("Select an event and fill in the participant's details.\n"
                + "Registration is refused if the event is full, the e-mail is invalid, or the e-mail is already registered.");
        help.setEditable(false);
        help.setOpaque(false);
        help.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        p.add(help, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildVenuesTab() {
        JPanel top = new JPanel(new GridLayout(2, 1));
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        addRow.add(new JLabel("New venue:")); addRow.add(venName);
        addRow.add(new JLabel("Capacity:")); addRow.add(venCap);
        addRow.add(btnAddVenue);
        JPanel allotRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        allotRow.add(new JLabel("Allot")); allotRow.add(allotEvent);
        allotRow.add(new JLabel("to")); allotRow.add(allotVenue);
        allotRow.add(btnAllot);
        top.add(addRow);
        top.add(allotRow);
        return tabPanel(top, venueModel, null);
    }

    private JPanel buildScheduleTab() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.add(new JLabel("Add session to:")); bottom.add(sessEvent);
        bottom.add(new JLabel("Title:")); bottom.add(sessTitle);
        bottom.add(new JLabel("Time (HH:mm):")); bottom.add(sessTime);
        bottom.add(btnAddSession);
        return tabPanel(null, scheduleModel, bottom);
    }

    private JPanel buildParticipantsTab() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(new JLabel("Event:")); top.add(listEvent);
        top.add(btnExport);
        return tabPanel(top, partModel, null);
    }

    private JPanel tabPanel(JComponent north, DefaultTableModel m, JComponent south) {
        JPanel p = new JPanel(new BorderLayout());
        if (north != null) p.add(north, BorderLayout.NORTH);
        p.add(new JScrollPane(new JTable(m)), BorderLayout.CENTER);
        if (south != null) p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    // ---------- ActionListener ----------

    @Override
    public void actionPerformed(ActionEvent e) {
        if (loading) return;
        Object src = e.getSource();
        try {
            if (src == btnAddEvent) createEvent();
            else if (src == btnRegister) registerParticipant();
            else if (src == btnAddVenue) addVenue();
            else if (src == btnAllot) allotVenue();
            else if (src == btnAddSession) addSession();
            else if (src == btnExport) exportParticipants();
            else if (src == listEvent) refreshParticipants();
        } catch (VenueConflictException ex) {
            warn("Venue conflict", ex.getMessage());
        } catch (DateTimeParseException ex) {
            warn("Invalid input", "Use dates as yyyy-mm-dd and times as HH:mm (24-hour).");
        } catch (NumberFormatException ex) {
            warn("Invalid input", "Capacity must be a whole number greater than zero.");
        } catch (IllegalArgumentException ex) {
            warn("Invalid input", ex.getMessage());
        } catch (SQLException ex) {
            warn("Database error", ex.getMessage());
        } catch (IOException ex) {
            warn("File error", ex.getMessage());
        }
    }

    // ---------- module actions ----------

    private void createEvent() throws SQLException {
        String name = evName.getText().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Event name is required.");
        LocalDate date = LocalDate.parse(evDate.getText().trim());
        LocalTime start = LocalTime.parse(evStart.getText().trim());
        LocalTime end = LocalTime.parse(evEnd.getText().trim());
        int cap = Integer.parseInt(evCap.getText().trim());
        if (!end.isAfter(start)) throw new IllegalArgumentException("End time must be after start time.");
        if (cap <= 0) throw new NumberFormatException();
        Event ev = Event.create((String) evType.getSelectedItem(), 0, name, date, start, end, cap);
        store.addEvent(ev);
        evName.setText("");
        note("Created " + ev);
        refreshAll();
    }

    private void registerParticipant() throws SQLException {
        Event ev = (Event) regEvent.getSelectedItem();
        if (ev == null) throw new IllegalArgumentException("Create an event first.");
        String name = regName.getText().trim();
        String email = regEmail.getText();
        String phone = regPhone.getText();
        if (name.isEmpty()) throw new IllegalArgumentException("Name is required.");
        if (!Participant.isValidEmail(email)) throw new IllegalArgumentException("Enter a valid e-mail address.");
        if (!Participant.isValidPhone(phone)) throw new IllegalArgumentException("Phone must be exactly 10 digits.");
        List<Participant> existing = store.getParticipants(ev.getId());
        if (existing.size() >= ev.getCapacity()) throw new IllegalArgumentException("This event is full (" + ev.getCapacity() + ").");
        for (Participant p : existing) {
            if (p.getEmail().equalsIgnoreCase(email.trim())) {
                throw new IllegalArgumentException("That e-mail is already registered for this event.");
            }
        }
        store.addParticipant(new Participant(0, ev.getId(), name, email, phone));
        regName.setText(""); regEmail.setText(""); regPhone.setText("");
        note(name + " registered for " + ev.getName());
        refreshParticipants();
    }

    private void addVenue() throws SQLException {
        String name = venName.getText().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Venue name is required.");
        int cap = Integer.parseInt(venCap.getText().trim());
        if (cap <= 0) throw new NumberFormatException();
        store.addVenue(name, cap);
        venName.setText("");
        note("Added venue " + name);
        refreshAll();
    }

    private void allotVenue() throws SQLException, VenueConflictException {
        Event ev = (Event) allotEvent.getSelectedItem();
        Venue v = (Venue) allotVenue.getSelectedItem();
        if (ev == null || v == null) throw new IllegalArgumentException("Pick both an event and a venue.");
        store.allotVenue(ev, v);
        note(v.getName() + " allotted to " + ev.getName());
        refreshAll();
    }

    private void addSession() throws SQLException {
        Event ev = (Event) sessEvent.getSelectedItem();
        if (ev == null) throw new IllegalArgumentException("Create an event first.");
        String title = sessTitle.getText().trim();
        if (title.isEmpty()) throw new IllegalArgumentException("Session title is required.");
        ev.addSession(title, LocalTime.parse(sessTime.getText().trim()));
        sessTitle.setText("");
        note("Session added to " + ev.getName());
        refreshSchedule();
    }

    private void exportParticipants() throws SQLException, IOException {
        Event ev = (Event) listEvent.getSelectedItem();
        if (ev == null) throw new IllegalArgumentException("No event selected.");
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(ev.getName().replaceAll("\\W+", "_") + "_participants.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) f = new File(f.getParentFile(), f.getName() + ".csv");
        try (PrintWriter out = new PrintWriter(new FileWriter(f))) {
            out.println("Name,Email,Phone");
            for (Participant p : store.getParticipants(ev.getId())) {
                out.println("\"" + p.getName().replace("\"", "\"\"") + "\"," + p.getEmail() + "," + p.getPhone());
            }
        }
        note("Exported to " + f.getAbsolutePath());
    }

    // ---------- refresh ----------

    private void refreshAll() {
        loading = true;
        try {
            List<Event> events = store.getEvents();
            List<Venue> venues = store.getVenues();

            eventModel.setRowCount(0);
            for (Event e : events) {
                eventModel.addRow(new Object[]{e.getId(), e.getName(), e.getType(), e.getDate(),
                        e.getStartTime(), e.getEndTime(), e.getCapacity(),
                        e.getVenue() == null ? "-" : e.getVenue().getName()});
            }
            venueModel.setRowCount(0);
            for (Venue v : venues) venueModel.addRow(new Object[]{v.getId(), v.getName(), v.getCapacity()});

            fill(regEvent, events);
            fill(allotEvent, events);
            fill(sessEvent, events);
            fill(listEvent, events);
            Venue keepV = (Venue) allotVenue.getSelectedItem();
            allotVenue.removeAllItems();
            for (Venue v : venues) allotVenue.addItem(v);
            if (keepV != null) for (Venue v : venues) if (v.getId() == keepV.getId()) allotVenue.setSelectedItem(v);
        } catch (SQLException ex) {
            warn("Database error", ex.getMessage());
        } finally {
            loading = false;
        }
        refreshSchedule();
        refreshParticipants();
    }

    private void fill(JComboBox<Event> box, List<Event> events) {
        Event keep = (Event) box.getSelectedItem();
        box.removeAllItems();
        for (Event e : events) box.addItem(e);
        if (keep != null) for (Event e : events) if (e.getId() == keep.getId()) box.setSelectedItem(e);
    }

    private void refreshSchedule() {
        try {
            List<Event> events = new ArrayList<>(store.getEvents());
            events.sort(Comparator.comparing(Event::getDate).thenComparing(Event::getStartTime));
            scheduleModel.setRowCount(0);
            for (Event e : events) {
                StringBuilder sb = new StringBuilder();
                for (Event.Session s : e.getSessions()) {
                    if (sb.length() > 0) sb.append("; ");
                    sb.append(s.getTime()).append(" ").append(s.getTitle());
                }
                scheduleModel.addRow(new Object[]{e.getDate(), e.getStartTime() + " - " + e.getEndTime(),
                        e.getName(), e.getType(), e.getVenue() == null ? "Not allotted" : e.getVenue().getName(),
                        sb.length() == 0 ? "-" : sb.toString()});
            }
        } catch (SQLException ex) {
            warn("Database error", ex.getMessage());
        }
    }

    private void refreshParticipants() {
        partModel.setRowCount(0);
        Event ev = (Event) listEvent.getSelectedItem();
        if (ev == null) return;
        try {
            for (Participant p : store.getParticipants(ev.getId())) {
                partModel.addRow(new Object[]{p.getId(), p.getName(), p.getEmail(), p.getPhone()});
            }
        } catch (SQLException ex) {
            warn("Database error", ex.getMessage());
        }
    }

    private void note(String msg) { status.setText(msg); }

    private void warn(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    // ---------- entry point ----------

    /** Reads db.properties (url, user, password) if present; otherwise falls back to memory. */
    static Store openStore() {
        File cfg = new File("db.properties");
        if (cfg.exists()) {
            Properties p = new Properties();
            try (FileInputStream in = new FileInputStream(cfg)) {
                p.load(in);
                return new JdbcStore(p.getProperty("url"), p.getProperty("user", ""), p.getProperty("password", ""));
            } catch (Exception ex) {
                System.err.println("Database unavailable (" + ex.getMessage() + "), using in-memory storage.");
            }
        }
        return new MemoryStore();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EventManagementSystem(openStore()).setVisible(true));
    }
}

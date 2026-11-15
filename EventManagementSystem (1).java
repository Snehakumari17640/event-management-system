import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Event Management System - Swing GUI (Unit 5: FlowLayout, BorderLayout, ActionListener).
 * Sidebar pages: Events | Registration | Venues | Schedule | Participants
 */
public class EventManagementSystem extends JFrame implements ActionListener {

    private final Store store;
    private boolean loading = false;

    // layout pieces
    private final CardLayout cards = new CardLayout();
    private final JPanel pages = new JPanel(cards);
    private final Map<String, Ui.NavButton> navs = new LinkedHashMap<>();
    private final Map<String, String> subtitles = new LinkedHashMap<>();
    private final JLabel pageTitle = new JLabel();
    private final JLabel pageSubtitle = new JLabel();
    private final JLabel status = new JLabel("Welcome! Start by adding a venue, then create an event.");
    private final JLabel statEvents = new JLabel("0");
    private final JLabel statVenues = new JLabel("0");
    private final JLabel statRegs = new JLabel("0");
    private final JLabel statOpen = new JLabel("0");

    // Module 1: Event creation
    private final JTextField evName = Ui.field(10);
    private final JTextField evDate = Ui.field(LocalDate.now().toString(), 7);
    private final JTextField evStart = Ui.field("10:00", 4);
    private final JTextField evEnd = Ui.field("12:00", 4);
    private final JTextField evCap = Ui.field("50", 3);
    private final JComboBox<String> evType = Ui.style(new JComboBox<>(new String[]{"Seminar", "Workshop", "Fest"}), 112);
    private final JButton btnAddEvent = Ui.primary("Create Event");
    private final DefaultTableModel eventModel = model("ID", "Name", "Type", "Date", "Start", "End", "Capacity", "Venue");

    // Module 2: Participant registration
    private final JComboBox<Event> regEvent = Ui.combo(230);
    private final JTextField regName = Ui.field(8);
    private final JTextField regEmail = Ui.field(13);
    private final JTextField regPhone = Ui.field(8);
    private final JButton btnRegister = Ui.primary("Register");

    // Module 3: Venue allotment
    private final JTextField venName = Ui.field(16);
    private final JTextField venCap = Ui.field("100", 5);
    private final JButton btnAddVenue = Ui.primary("Add Venue");
    private final JComboBox<Event> allotEvent = Ui.combo(260);
    private final JComboBox<Venue> allotVenue = Ui.combo(300);
    private final JButton btnAllot = Ui.success("Allot Venue");
    private final DefaultTableModel venueModel = model("ID", "Venue", "Capacity");

    // Module 4: Schedule view
    private final DefaultTableModel scheduleModel = model("Date", "Time", "Event", "Type", "Venue", "Sessions");
    private final JComboBox<Event> sessEvent = Ui.combo(260);
    private final JTextField sessTitle = Ui.field(16);
    private final JTextField sessTime = Ui.field("10:30", 5);
    private final JButton btnAddSession = Ui.primary("Add Session");

    // Module 5: Participant list
    private final JComboBox<Event> listEvent = Ui.combo(280);
    private final DefaultTableModel partModel = model("ID", "Name", "Email", "Phone");
    private final JButton btnExport = Ui.success("Export to CSV...");

    public EventManagementSystem(Store store) {
        super("Event Management System");
        this.store = store;
        getContentPane().setBackground(Ui.BG);
        setLayout(new BorderLayout());

        // ----- pages -----
        pages.setBackground(Ui.BG);
        pages.setBorder(new EmptyBorder(8, 26, 12, 26));
        addPage("Events", "Create seminars, workshops and fests", buildEventsPage());
        addPage("Registration", "Register participants for an event", buildRegistrationPage());
        addPage("Venues", "Add venues and allot them to events", buildVenuesPage());
        addPage("Schedule", "Everything happening, sorted by date and time", buildSchedulePage());
        addPage("Participants", "See who has registered and export the list", buildParticipantsPage());

        add(buildSidebar(), BorderLayout.WEST);
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.add(buildHeader(), BorderLayout.NORTH);
        main.add(pages, BorderLayout.CENTER);
        main.add(buildStatusBar(), BorderLayout.SOUTH);
        add(main, BorderLayout.CENTER);

        // ----- listeners -----
        for (JButton b : new JButton[]{btnAddEvent, btnRegister, btnAddVenue, btnAllot, btnAddSession, btnExport}) {
            b.addActionListener(this);
        }
        listEvent.addActionListener(this);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(1100, 640));
        setLocationRelativeTo(null);

        refreshAll();
        showPage("Events");

        // Unit 3: reminder thread, polls every 30s, reminds 15 minutes ahead
        ReminderThread reminder = new ReminderThread(store, 15, 30_000, msg ->
                SwingUtilities.invokeLater(() -> {
                    status.setText(msg);
                    JOptionPane.showMessageDialog(this, msg, "Event Reminder", JOptionPane.INFORMATION_MESSAGE);
                }));
        reminder.start();
    }

    // ---------- shell: sidebar, header, status bar ----------

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Ui.SIDEBAR);
        side.setPreferredSize(new Dimension(230, 100));

        JPanel brand = new JPanel(new GridLayout(2, 1));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(26, 24, 22, 16));
        JLabel name = new JLabel("EventHub");
        name.setFont(Ui.BASE.deriveFont(Font.BOLD, 24f));
        name.setForeground(Color.WHITE);
        JLabel tag = new JLabel("Event Management System");
        tag.setFont(Ui.SMALL);
        tag.setForeground(new Color(0xA5B4FC));
        brand.add(name);
        brand.add(tag);
        side.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        for (String key : subtitles.keySet()) {
            Ui.NavButton b = new Ui.NavButton(key);
            b.addActionListener(e -> showPage(key));
            navs.put(key, b);
            nav.add(b);
            nav.add(Box.createVerticalStrut(4));
        }
        side.add(nav, BorderLayout.CENTER);

        JLabel storage = new JLabel("<html><div style='width:170px'>Storage: " + store.getModeName() + "</div></html>");
        storage.setFont(Ui.SMALL);
        storage.setForeground(new Color(0xA5B4FC));
        storage.setBorder(new EmptyBorder(14, 24, 20, 12));
        side.add(storage, BorderLayout.SOUTH);
        return side;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(22, 26, 12, 26));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        pageTitle.setFont(Ui.TITLE);
        pageTitle.setForeground(Ui.TEXT);
        pageSubtitle.setFont(Ui.BASE);
        pageSubtitle.setForeground(Ui.MUTED);
        titles.add(pageTitle);
        titles.add(pageSubtitle);
        header.add(titles, BorderLayout.WEST);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        chips.setOpaque(false);
        chips.add(chip(statEvents, "Events"));
        chips.add(chip(statVenues, "Venues"));
        chips.add(chip(statRegs, "Registrations"));
        chips.add(chip(statOpen, "No venue yet"));
        header.add(chips, BorderLayout.EAST);
        return header;
    }

    private JPanel chip(JLabel value, String caption) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(new Color(0xE5E7EB), 1, true), new EmptyBorder(6, 14, 6, 14)));
        value.setFont(Ui.BASE.deriveFont(Font.BOLD, 20f));
        value.setForeground(Ui.ACCENT);
        JLabel cap = new JLabel(caption);
        cap.setFont(Ui.SMALL);
        cap.setForeground(Ui.MUTED);
        p.add(value);
        p.add(cap);
        return p;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE5E7EB)), new EmptyBorder(8, 26, 8, 26)));
        status.setFont(Ui.BASE);
        status.setForeground(Ui.MUTED);
        bar.add(status, BorderLayout.CENTER);
        return bar;
    }

    private void addPage(String key, String subtitle, JComponent page) {
        subtitles.put(key, subtitle);
        pages.add(page, key);
    }

    private void showPage(String key) {
        cards.show(pages, key);
        pageTitle.setText(key);
        pageSubtitle.setText(subtitles.get(key));
        for (Map.Entry<String, Ui.NavButton> e : navs.entrySet()) e.getValue().setActive(e.getKey().equals(key));
    }

    // ---------- page builders ----------

    private JPanel buildEventsPage() {
        JPanel form = Ui.row(
                Ui.labeled("Event name", evName),
                Ui.labeled("Type", evType),
                Ui.labeled("Date (yyyy-mm-dd)", evDate),
                Ui.labeled("Start (HH:mm)", evStart),
                Ui.labeled("End (HH:mm)", evEnd),
                Ui.labeled("Capacity", evCap),
                Ui.labeled(" ", btnAddEvent));
        return page(Ui.card("Create a new event", form), Ui.scroll(new JTable(eventModel), 50, 230, 100, 110, 80, 80, 90, 170), null);
    }

    private JPanel buildRegistrationPage() {
        JPanel form = Ui.row(
                Ui.labeled("Event", regEvent),
                Ui.labeled("Participant name", regName),
                Ui.labeled("Email", regEmail),
                Ui.labeled("Phone (10 digits)", regPhone),
                Ui.labeled(" ", btnRegister));
        JLabel help = new JLabel("<html><div style='width:520px;line-height:1.6'>"
                + "<b>1.</b> Pick the event the person is joining.<br>"
                + "<b>2.</b> Enter their name, a valid email and a 10 digit phone number.<br>"
                + "<b>3.</b> Click Register. You will be told if the event is full or the email is already used.<br><br>"
                + "Registered people appear on the Participants page."
                + "</div></html>");
        help.setFont(Ui.BASE);
        help.setForeground(Ui.MUTED);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel helpCard = Ui.card("How it works", help);
        top.add(helpCard, BorderLayout.NORTH);
        return page(Ui.card("Register a participant", form), top, null);
    }

    private JPanel buildVenuesPage() {
        JPanel addVenue = Ui.row(
                Ui.labeled("Venue name", venName),
                Ui.labeled("Capacity", venCap),
                Ui.labeled(" ", btnAddVenue));
        JPanel allot = Ui.row(
                Ui.labeled("Event", allotEvent),
                Ui.labeled("Venue", allotVenue),
                Ui.labeled(" ", btnAllot));
        JPanel top = new JPanel(new GridLayout(2, 1, 0, 14));
        top.setOpaque(false);
        top.add(Ui.card("Add a venue", addVenue));
        top.add(Ui.card("Allot a venue to an event", allot));
        return page(top, Ui.scroll(new JTable(venueModel), 60, 320, 120), null);
    }

    private JPanel buildSchedulePage() {
        JPanel form = Ui.row(
                Ui.labeled("Event", sessEvent),
                Ui.labeled("Session title", sessTitle),
                Ui.labeled("Time (HH:mm)", sessTime),
                Ui.labeled(" ", btnAddSession));
        return page(null, Ui.scroll(new JTable(scheduleModel), 120, 125, 170, 100, 150, 232), Ui.card("Add a session to an event", form));
    }

    private JPanel buildParticipantsPage() {
        JPanel form = Ui.row(Ui.labeled("Event", listEvent), Ui.labeled(" ", btnExport));
        return page(Ui.card("Choose an event", form), Ui.scroll(new JTable(partModel), 60, 220, 280, 160), null);
    }

    private JPanel page(JComponent north, JComponent center, JComponent south) {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        if (north != null) p.add(north, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
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
        refreshAll();
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
            int open = 0, regs = 0;
            for (Event e : events) {
                if (e.getVenue() == null) open++;
                regs += store.getParticipants(e.getId()).size();
                eventModel.addRow(new Object[]{e.getId(), e.getName(), e.getType(), e.getDate(),
                        e.getStartTime(), e.getEndTime(), e.getCapacity(),
                        e.getVenue() == null ? "-" : e.getVenue().getName()});
            }
            venueModel.setRowCount(0);
            for (Venue v : venues) venueModel.addRow(new Object[]{v.getId(), v.getName(), v.getCapacity()});

            statEvents.setText(String.valueOf(events.size()));
            statVenues.setText(String.valueOf(venues.size()));
            statRegs.setText(String.valueOf(regs));
            statOpen.setText(String.valueOf(open));

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

    private void note(String msg) {
        status.setForeground(Ui.GREEN);
        status.setText(msg);
    }

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
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // fall back to the default look and feel
        }
        SwingUtilities.invokeLater(() -> new EventManagementSystem(openStore()).setVisible(true));
    }
}

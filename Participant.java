/** Unit 1: Participant class (uses String methods for cleaning/validation). */
public class Participant {
    private int id;
    private final int eventId;
    private final String name;
    private final String email;
    private final String phone;

    public Participant(int id, int eventId, String name, String email, String phone) {
        this.id = id;
        this.eventId = eventId;
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.phone = phone.trim();
    }

    public static boolean isValidEmail(String email) {
        return email.trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    }

    public static boolean isValidPhone(String phone) {
        return phone.trim().matches("\\d{10}");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEventId() { return eventId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}

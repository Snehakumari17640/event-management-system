import java.time.LocalDate;
import java.time.LocalTime;

public class Workshop extends Event {
    public Workshop(int id, String name, LocalDate d, LocalTime s, LocalTime e, int cap) {
        super(id, name, d, s, e, cap);
    }
    @Override public String getType() { return "Workshop"; }
    @Override public String describe() { return "Hands-on event with limited seats."; }
}

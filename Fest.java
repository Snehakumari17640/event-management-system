import java.time.LocalDate;
import java.time.LocalTime;

public class Fest extends Event {
    public Fest(int id, String name, LocalDate d, LocalTime s, LocalTime e, int cap) {
        super(id, name, d, s, e, cap);
    }
    @Override public String getType() { return "Fest"; }
    @Override public String describe() { return "Large event made of many sessions."; }
}

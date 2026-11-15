import java.time.LocalDate;
import java.time.LocalTime;

public class Seminar extends Event {
    public Seminar(int id, String name, LocalDate d, LocalTime s, LocalTime e, int cap) {
        super(id, name, d, s, e, cap);
    }
    @Override public String getType() { return "Seminar"; }
    @Override public String describe() { return "Talk-based event with a speaker and Q&A."; }
}

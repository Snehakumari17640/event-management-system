import java.time.LocalDate;
import java.time.LocalTime;

/** Unit 2: interface implemented by anything that occupies a venue for a time slot. */
public interface Schedulable {
    LocalDate getDate();
    LocalTime getStartTime();
    LocalTime getEndTime();
    Venue getVenue();

    /** True if both items fall on the same day and their time ranges overlap. */
    default boolean overlaps(Schedulable other) {
        return getDate().equals(other.getDate())
                && getStartTime().isBefore(other.getEndTime())
                && other.getStartTime().isBefore(getEndTime());
    }
}

/** Unit 3: custom checked exception thrown when a venue cannot be allotted. */
public class VenueConflictException extends Exception {
    public VenueConflictException(String message) {
        super(message);
    }
}

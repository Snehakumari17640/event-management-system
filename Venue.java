/** Unit 1: Venue class. */
public class Venue {
    private final int id;
    private final String name;
    private final int capacity;

    public Venue(int id, String name, int capacity) {
        this.id = id;
        this.name = name.trim();
        this.capacity = capacity;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }

    @Override
    public String toString() {
        return name + " (capacity " + capacity + ")";
    }
}

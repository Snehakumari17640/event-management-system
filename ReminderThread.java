import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/** Unit 3: background thread that announces events starting soon. */
public class ReminderThread extends Thread {
    private final Store store;
    private final Consumer<String> sink;
    private final int minutesBefore;
    private final long pollMillis;
    private final Set<Integer> reminded = new HashSet<>();

    public ReminderThread(Store store, int minutesBefore, long pollMillis, Consumer<String> sink) {
        super("event-reminder");
        setDaemon(true);
        this.store = store;
        this.minutesBefore = minutesBefore;
        this.pollMillis = pollMillis;
        this.sink = sink;
    }

    /** One scan of all events; separated out so it can be tested. */
    public void checkOnce(LocalDateTime now) throws Exception {
        for (Event e : store.getEvents()) {
            LocalDateTime start = LocalDateTime.of(e.getDate(), e.getStartTime());
            long mins = Duration.between(now, start).toMinutes();
            if (!now.isAfter(start) && mins <= minutesBefore && reminded.add(e.getId())) {
                String where = e.getVenue() == null ? "venue not allotted" : e.getVenue().getName();
                sink.accept("Reminder: " + e.getName() + " starts at " + e.getStartTime() + " (" + where + ")");
            }
        }
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                checkOnce(LocalDateTime.now());
                Thread.sleep(pollMillis);
            } catch (InterruptedException ie) {
                return;
            } catch (Exception ex) {
                System.err.println("Reminder check failed: " + ex.getMessage());
                try { Thread.sleep(pollMillis); } catch (InterruptedException ie) { return; }
            }
        }
    }
}

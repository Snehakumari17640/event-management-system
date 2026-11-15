# Event Management System (CSE-D OOPJ Project)

Team: Gurpreet (Leader, 2510001346), Sneha Kumari (2510001368), Aditya Raj (2510001332), Shivam (2510001343)

## Run
    javac -d out src/*.java
    java -cp out EventManagementSystem                      # in-memory mode
    java -cp out:lib/mysql-connector-j.jar EventManagementSystem   # with MySQL (Windows: use ; instead of :)

For MySQL: put the connector jar in `lib/`, copy `db.properties.example` to `db.properties`, edit it, run from this folder.
If no db.properties exists or the DB is unreachable, the app runs with in-memory storage.

## Modules -> files
1. Event creation - Events tab (Event, Seminar, Workshop, Fest)
2. Participant registration - Registration tab (Participant)
3. Venue allotment - Venues tab (Venue, VenueConflictException)
4. Schedule view - Schedule tab (Schedulable, Event.Session)
5. Participant list - Participants tab (CSV export via File)

## Syllabus mapping
- Unit 1: Event, Participant, Venue classes; String methods (trim, matches, equalsIgnoreCase); File class (CSV export)
- Unit 2: Event -> Seminar/Workshop/Fest inheritance; interface Schedulable; inner class Event.Session
- Unit 3: VenueConflictException; ReminderThread (thread for event reminders)
- Unit 4: JdbcStore uses PreparedStatement for all event and participant queries
- Unit 5: Swing FlowLayout + BorderLayout, ActionListener

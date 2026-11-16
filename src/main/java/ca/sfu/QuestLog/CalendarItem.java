
package ca.sfu.QuestLog;

import java.time.Instant;

// Epic 3: one entry on the merged calendar.
// source is "google", "sample" (demo data when Google is not connected) or "task".
public record CalendarItem(String title, Instant start, Instant end,
                           boolean allDay, String source) {}

package ca.sfu.QuestLog;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

// Epic 3: how often a task repeats once it is completed.
public enum Recurrence {
    NONE("No"),
    DAILY("Daily"),
    WEEKLY("Every week"),
    MONTHLY("Monthly");

    // Picking all 7 days is the same as plain "Daily", so the picker allows at most 6.
    public static final int MAX_DAILY_DAYS = 6;

    private final String label;

    Recurrence(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // Next due date after `due`. For DAILY, `days` limits which weekdays count
    // (empty means every day). Returns null for NONE.
    public Instant next(Instant due, Set<DayOfWeek> days, ZoneId zone) {
        ZonedDateTime d = due.atZone(zone);
        return switch (this) {
            case NONE -> null;
            case WEEKLY -> d.plusWeeks(1).toInstant();
            case MONTHLY -> d.plusMonths(1).toInstant();
            case DAILY -> {
                ZonedDateTime n = d.plusDays(1);
                if (days != null && !days.isEmpty()) {
                    while (!days.contains(n.getDayOfWeek())) {
                        n = n.plusDays(1);
                    }
                }
                yield n.toInstant();
            }
        };
    }

    // "MONDAY,WEDNESDAY" <-> {MONDAY, WEDNESDAY}, for storing the days in one column.
    public static Set<DayOfWeek> parseDays(String stored) {
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        if (stored != null && !stored.isBlank()) {
            for (String s : stored.split(",")) {
                days.add(DayOfWeek.valueOf(s.trim()));
            }
        }
        return days;
    }

    public static String joinDays(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            return null;
        }
        return EnumSet.copyOf(days).stream().map(DayOfWeek::name).collect(Collectors.joining(","));
    }

    // Label for the task list, e.g. "Daily (Mon, Wed)".
    public String describe(Set<DayOfWeek> days) {
        if (this == DAILY && days != null && !days.isEmpty()) {
            return label + " (" + EnumSet.copyOf(days).stream()
                    .map(d -> d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .collect(Collectors.joining(", ")) + ")";
        }
        return label;
    }
}

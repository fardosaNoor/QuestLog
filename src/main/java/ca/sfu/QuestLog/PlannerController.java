
package ca.sfu.QuestLog;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Epic 3: Calendar and Task Management (tasks, urgency, Google Calendar, study plan).
@Controller
public class PlannerController {

    private static final int CALENDAR_DAYS = 14;
    private static final DateTimeFormatter DUE_FORMAT =
            DateTimeFormatter.ofPattern("EEE MMM d, h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    // What the template needs to render.
    public record TaskRow(Task task, Urgency urgency, String dueLabel) {}
    public record CalRow(String time, String title, String source) {}
    public record DayGroup(String label, List<CalRow> rows) {}
    public record PlanRow(StudyPlanner.PlanItem item, String dueLabel) {}

    private final TaskRepository taskRepository;
    private final CourseRepository courseRepository;
    private final GoogleCalendarService google;

    @Value("${PLANNER_ZONE:America/Vancouver}")
    private String zoneName;

    public PlannerController(TaskRepository taskRepository,
                             CourseRepository courseRepository,
                             GoogleCalendarService google) {
        this.taskRepository = taskRepository;
        this.courseRepository = courseRepository;
        this.google = google;
    }

    // ---------------- Planner page ----------------

    @GetMapping("/planner")
    public String planner(Authentication authentication,
                          @RequestParam(required = false) String plan,
                          Model model) {

        String email = authentication.getName();
        ZoneId zone = ZoneId.of(zoneName);
        Instant now = Instant.now();

        List<Task> tasks = taskRepository.findByStudentEmail(email);

        // Tasks: unfinished first, closest due date first (overdue ends up on top).
        List<TaskRow> rows = tasks.stream()
                .sorted(Comparator.comparing(Task::isDone).thenComparing(Task::getDueAt))
                .map(t -> new TaskRow(t, Urgency.of(t.getDueAt(), now), format(t.getDueAt(), zone)))
                .toList();

        model.addAttribute("rows", rows);
        model.addAttribute("courses", courseRepository.findByStudentEmailOrderByCodeAsc(email));
        model.addAttribute("configured", google.isConfigured());
        model.addAttribute("connected", google.isConnected(email));

        // Merged calendar: Google (or sample) events + task due dates.
        try {
            model.addAttribute("days", groupByDay(calendarItems(email, zone, now), zone));
        } catch (Exception e) {
            model.addAttribute("calendarError",
                    "Could not load Google Calendar events. Try disconnecting and reconnecting.");
            model.addAttribute("days", List.<DayGroup>of());
        }

        if (plan != null) {
            List<PlanRow> planRows = StudyPlanner.build(tasks, zone, now).stream()
                    .map(p -> new PlanRow(p, format(p.due(), zone)))
                    .toList();
            model.addAttribute("plan", planRows);
        }

        return "planner";
    }

    // ---------------- Tasks ----------------

    @PostMapping("/tasks")
    public String addTask(Authentication authentication,
                          @RequestParam String title,
                          @RequestParam String due,
                          @RequestParam(required = false) String courseId,
                          @RequestParam(required = false) String estimateHours,
                          RedirectAttributes redirect) {

        String email = authentication.getName();
        title = title.trim();

        if (title.isBlank()) {
            return fail(redirect, "Please enter a task title.");
        }
        if (title.length() > 120) {
            return fail(redirect, "Task title must be 120 characters or fewer.");
        }

        Instant dueAt;
        try {
            dueAt = LocalDateTime.parse(due.trim()).atZone(ZoneId.of(zoneName)).toInstant();
        } catch (DateTimeParseException e) {
            return fail(redirect, "Please choose a valid due date.");
        }

        Course course = null;
        if (courseId != null && !courseId.isBlank()) {
            try {
                long id = Long.parseLong(courseId.trim());
                course = courseRepository.findByStudentEmailOrderByCodeAsc(email).stream()
                        .filter(c -> c.getId() == id)
                        .findFirst()
                        .orElse(null);
            } catch (NumberFormatException ignored) {
                // falls through to the error below
            }
            if (course == null) {
                return fail(redirect, "Selected course was not found.");
            }
        }

        Double hours = null;
        if (estimateHours != null && !estimateHours.isBlank()) {
            try {
                hours = Double.parseDouble(estimateHours.trim());
            } catch (NumberFormatException e) {
                return fail(redirect, "Estimated hours must be a number.");
            }
            if (hours <= 0 || hours > 200) {
                return fail(redirect, "Estimated hours must be between 0 and 200.");
            }
        }

        taskRepository.save(new Task(title, dueAt, course, hours, email));
        redirect.addFlashAttribute("taskMessage", "Task added!");
        return "redirect:/planner";
    }

    @PostMapping("/tasks/{id}/toggle")
    public String toggleTask(Authentication authentication, @PathVariable Long id) {
        taskRepository.findByIdAndStudentEmail(id, authentication.getName()).ifPresent(task -> {
            task.toggleDone();
            taskRepository.save(task);
        });
        return "redirect:/planner";
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(Authentication authentication, @PathVariable Long id) {
        taskRepository.findByIdAndStudentEmail(id, authentication.getName())
                .ifPresent(taskRepository::delete);
        return "redirect:/planner";
    }

    // ---------------- Google Calendar connection ----------------

    @GetMapping("/auth/google")
    public String connectGoogle(HttpSession session, RedirectAttributes redirect) {
        if (!google.isConfigured()) {
            return fail(redirect, "Google Calendar is not set up on this server yet.");
        }
        String state = UUID.randomUUID().toString();
        session.setAttribute("googleOAuthState", state);
        return "redirect:" + google.authUrl(state);
    }

    @GetMapping("/auth/google/callback")
    public String googleCallback(Authentication authentication,
                                 HttpSession session,
                                 @RequestParam(required = false) String code,
                                 @RequestParam(required = false) String state,
                                 @RequestParam(required = false) String error,
                                 RedirectAttributes redirect) {

        Object expected = session.getAttribute("googleOAuthState");
        session.removeAttribute("googleOAuthState");

        if (error != null || code == null) {
            return fail(redirect, "Google Calendar was not connected.");
        }
        if (expected == null || !expected.equals(state)) {
            return fail(redirect, "Google sign-in could not be verified. Please try again.");
        }

        try {
            google.handleCallback(authentication.getName(), code);
            redirect.addFlashAttribute("taskMessage", "Google Calendar connected!");
        } catch (Exception e) {
            return fail(redirect, "Could not connect Google Calendar. Please try again.");
        }
        return "redirect:/planner";
    }

    @PostMapping("/calendar/disconnect")
    public String disconnectGoogle(Authentication authentication) {
        google.disconnect(authentication.getName());
        return "redirect:/planner";
    }

    // ---------------- Shared calendar data ----------------
    // Epic 1 (course conflict checking) can use this same merged data.

    @GetMapping("/api/calendar/events")
    @ResponseBody
    public List<CalendarItem> calendarEvents(Authentication authentication) {
        return calendarItems(authentication.getName(), ZoneId.of(zoneName), Instant.now());
    }

    // ---------------- Helpers ----------------

    private List<CalendarItem> calendarItems(String email, ZoneId zone, Instant now) {
        Instant from = now.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant();
        Instant to = from.plusSeconds(CALENDAR_DAYS * 24L * 3600L);

        List<CalendarItem> items = new ArrayList<>(google.events(email, from, to, zone));

        for (Task t : taskRepository.findByStudentEmail(email)) {
            if (!t.getDueAt().isBefore(from) && !t.getDueAt().isAfter(to)) {
                String course = t.getCourse() != null ? " (" + t.getCourse().getCode() + ")" : "";
                items.add(new CalendarItem(t.getTitle() + course, t.getDueAt(), t.getDueAt(), false, "task"));
            }
        }

        items.sort(Comparator.comparing(CalendarItem::start));
        return items;
    }

    private List<DayGroup> groupByDay(List<CalendarItem> items, ZoneId zone) {
        TreeMap<LocalDate, List<CalRow>> byDay = new TreeMap<>();
        for (CalendarItem i : items) {
            LocalDate day = i.start().atZone(zone).toLocalDate();
            String time = i.allDay() ? "All day" : TIME_FORMAT.format(i.start().atZone(zone));
            byDay.computeIfAbsent(day, d -> new ArrayList<>())
                    .add(new CalRow(time, i.title(), i.source()));
        }
        List<DayGroup> groups = new ArrayList<>();
        byDay.forEach((day, rows) -> groups.add(new DayGroup(DAY_FORMAT.format(day), rows)));
        return groups;
    }

    private String format(Instant instant, ZoneId zone) {
        return DUE_FORMAT.format(instant.atZone(zone));
    }

    private String fail(RedirectAttributes redirect, String message) {
        redirect.addFlashAttribute("taskError", message);
        return "redirect:/planner";
    }
}

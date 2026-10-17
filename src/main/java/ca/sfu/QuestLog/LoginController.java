
package ca.sfu.QuestLog;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final AppUserRepository repository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginController(
            AppUserRepository repository,
            CourseRepository courseRepository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        model.addAttribute(
                "courses",
                courseRepository.findByStudentEmailOrderByCodeAsc(
                        authentication.getName()));

        return "dashboard";
    }

    @GetMapping("/courses/new")
    public String showAddCourse() {
        return "add-course";
    }

    @PostMapping("/courses")
    public String addCourse(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam String semester,
            Authentication authentication) {

        code = code.trim().toUpperCase();
        name = name.trim();
        semester = semester.trim();

        if (code.isBlank() || name.isBlank() || semester.isBlank()) {
            return "redirect:/courses/new?error";
        }

        Course course = new Course(
                code,
                name,
                semester,
                authentication.getName());

        courseRepository.save(course);

        return "redirect:/dashboard?added";
    }

    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        name = name.trim();
        email = email.trim().toLowerCase();

        if (name.isBlank() || email.isBlank()
                || password.isBlank()) {
            model.addAttribute(
                    "error", "Please fill in all fields.");
            return "register";
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute(
                    "error", "Enter a valid email address.");
            return "register";
        }

        if (password.length() < 8) {
            model.addAttribute(
                    "error", "Password must be at least 8 characters.");
            return "register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute(
                    "error", "Passwords do not match.");
            return "register";
        }

        if (repository.existsByEmail(email)) {
            model.addAttribute(
                    "error",
                    "An account with this email already exists.");
            return "register";
        }

        AppUser account = new AppUser(
                name,
                email,
                passwordEncoder.encode(password));

        repository.save(account);

        return "redirect:/login?registered";
    }
}

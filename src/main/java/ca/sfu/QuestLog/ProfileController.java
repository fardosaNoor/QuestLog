package ca.sfu.QuestLog;

import java.io.IOException;
import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ProfileController {

    private final AppUserRepository repository;
    private final TaskRepository taskRepository;
    private final UserProfileRepository profileRepository;
    private final StreakService streakService;
    private final ProfilePhotoService photoService;

    // One constructor with all dependencies
    public ProfileController(
            AppUserRepository repository,
            UserProfileRepository profileRepository,
            TaskRepository taskRepository,
            StreakService streakService,
            ProfilePhotoService photoService) {

        this.repository = repository;
        this.profileRepository = profileRepository;
        this.taskRepository = taskRepository;
        this.streakService = streakService;
        this.photoService = photoService;
    }

    // Display the logged-in student's profile
    @GetMapping("/profile")
    public String viewProfile(
            Authentication authentication,
            Model model) {

        AppUser user = getCurrentUser(authentication);

        UserProfile profile = profileRepository
                .findById(user.getId())
                .orElseGet(() -> new UserProfile(user.getId()));

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("theme", user.getTheme());

        // Study streak tracking
        int currentStreak =
                streakService.getCurrentStreak(user.getEmail());

        int goal = profile.getStreakGoal();

        int progressPercent = (int) Math.min(
                100,
                Math.round(currentStreak * 100.0 / goal)
        );

        int daysRemaining =
                Math.max(0, goal - currentStreak);

        model.addAttribute("currentStreak", currentStreak);
        model.addAttribute("progressPercent", progressPercent);
        model.addAttribute("daysRemaining", daysRemaining);

        // First Quest badge
        List<Task> tasks =
                taskRepository.findByStudentEmail(user.getEmail());

        long completedCount = tasks.stream()
                .filter(Task::isDone)
                .count();

        boolean firstQuestUnlocked =
                BadgeCalculator.hasFirstQuest(completedCount);

        model.addAttribute(
                "firstQuestUnlocked",
                firstQuestUnlocked
        );

        return "profile";
    }

    // Display the edit profile form
    @GetMapping("/profile/edit")
    public String editProfile(
            Authentication authentication,
            Model model) {

        AppUser user = getCurrentUser(authentication);

        model.addAttribute("user", user);
        model.addAttribute("theme", user.getTheme());

        return "edit-profile";
    }

    // Save the student's updated name
    @PostMapping("/profile/edit")
    public String updateProfile(
            Authentication authentication,
            @RequestParam("name") String name) {

        AppUser user = getCurrentUser(authentication);

        String cleanName = name.trim();

        if (cleanName.isEmpty() || cleanName.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Name must contain 1 to 100 characters");
        }

        user.setName(cleanName);
        repository.save(user);

        return "redirect:/profile";
    }

    // Save the student's streak goal
    @PostMapping("/profile/streak-goal")
    public String updateStreakGoal(
            Authentication authentication,
            @RequestParam("streakGoal") int streakGoal) {

        if (streakGoal != 7
                && streakGoal != 14
                && streakGoal != 30) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid streak goal");
        }

        AppUser user = getCurrentUser(authentication);

        UserProfile profile = profileRepository
                .findById(user.getId())
                .orElseGet(() -> new UserProfile(user.getId()));

        profile.setStreakGoal(streakGoal);
        profileRepository.save(profile);

        return "redirect:/profile";
    }

    // Upload profile picture
    @PostMapping("/profile/photo")
    public String uploadProfilePhoto(
            Authentication authentication,
            @RequestParam("photo") MultipartFile photo)
            throws IOException {

        AppUser user = getCurrentUser(authentication);

        try {
            photoService.savePhoto(user.getId(), photo);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage());
        }

        return "redirect:/profile";
    }

    // Retrieve profile picture
    @GetMapping("/profile/photo")
    public ResponseEntity<byte[]> viewProfilePhoto(
            Authentication authentication)
            throws IOException {

        AppUser user = getCurrentUser(authentication);

        byte[] photo =
                photoService.getPhoto(user.getId());

        if (photo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noStore())
                .body(photo);
    }

    // Find currently logged-in student
    private AppUser getCurrentUser(
            Authentication authentication) {

        String email = authentication.getName();

        return repository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"));
    }
}
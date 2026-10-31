package ca.sfu.QuestLog;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ProfileController {

    private final AppUserRepository repository;

    public ProfileController(AppUserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {

        AppUser user = getCurrentUser(authentication);

        model.addAttribute("user", user);

        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Authentication authentication, Model model) {

        AppUser user = getCurrentUser(authentication);

        model.addAttribute("user", user);

        return "edit-profile";
    }

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

    private AppUser getCurrentUser(Authentication authentication) {

        String email = authentication.getName();

        return repository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"));
    }
}

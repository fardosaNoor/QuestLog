package ca.sfu.QuestLog;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShopController {

    private final AppUserRepository userRepository;

    public ShopController(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/shop")
    public String shop(
            Authentication authentication,
            Model model) {

        AppUser user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();

        model.addAttribute("coins", user.getCoins());

        return "shop";
    }
}
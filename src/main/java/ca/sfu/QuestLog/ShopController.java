package ca.sfu.QuestLog;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ShopController {

    private final AppUserRepository userRepository;
    private final OwnedItemRepository ownedItemRepository;

    public ShopController(
            AppUserRepository userRepository,
            OwnedItemRepository ownedItemRepository) {
        this.userRepository = userRepository;
        this.ownedItemRepository = ownedItemRepository;
    }

    @GetMapping("/shop")
    public String shop(
            Authentication authentication,
            Model model) {

        AppUser user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();

        List<String> ownedItems = new ArrayList<>();
        for (OwnedItem ownedItem : ownedItemRepository.findByStudentEmail(user.getEmail())) {
            ownedItems.add(ownedItem.getItemName());
        }

        model.addAttribute("coins", user.getCoins());
        model.addAttribute("ownedItems", ownedItems);
        model.addAttribute("theme", user.getTheme());

        return "shop";
    }

    @PostMapping("/shop/buy")
    public String buyItem(
            Authentication authentication,
            @RequestParam("itemName") String itemName) {

        if (!itemName.equals("theme-green") && !itemName.equals("theme-red")
                && !itemName.equals("theme-pink") && !itemName.equals("theme-blue")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid item");
        }

        int price = 10;

        AppUser user = userRepository.findByEmail(authentication.getName()).orElseThrow();

        if (ownedItemRepository.existsByItemNameAndStudentEmail(itemName, user.getEmail())) {
            return "redirect:/shop?owned";
        }

        if (user.getCoins() < price) {
            return "redirect:/shop?notEnoughCoins";
        }

        user.setCoins(user.getCoins() - price);
        userRepository.save(user);

        ownedItemRepository.save(new OwnedItem(itemName, user.getEmail()));

        return "redirect:/shop?bought";
    }

    @PostMapping("/shop/use")
    public String useItem(
            Authentication authentication,
            @RequestParam("itemName") String itemName) {

        AppUser user = userRepository.findByEmail(authentication.getName()).orElseThrow();

        if (!ownedItemRepository.existsByItemNameAndStudentEmail(itemName, user.getEmail())) {
            return "redirect:/shop?notOwned";
        }

        user.setTheme(itemName);
        userRepository.save(user);

        return "redirect:/shop?used";
    }
}
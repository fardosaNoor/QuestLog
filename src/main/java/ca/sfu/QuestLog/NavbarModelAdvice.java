package ca.sfu.QuestLog;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// Adds the logged-in user's coin balance to every page, so the shared navbar
// (templates/fragments/navbar.html) can always show it.
// Controllers that set "coins" themselves (dashboard, shop) still win, since they run after this.
@ControllerAdvice
public class NavbarModelAdvice {

    private final AppUserRepository users;

    public NavbarModelAdvice(AppUserRepository users) {
        this.users = users;
    }

    @ModelAttribute("coins")
    public Integer coins(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null; // login / register pages: no navbar, no coins
        }
        return users.findByEmail(authentication.getName())
                .map(AppUser::getCoins)
                .orElse(null);
    }
}

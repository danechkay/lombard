package ru.lombard.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.lombard.service.CartService;
import ru.lombard.service.CurrentUserService;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final CurrentUserService currentUserService;
    private final CartService cartService;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        currentUserService.getCurrentUser().ifPresent(user -> {
            model.addAttribute("cartCount", cartService.getCartCount(user.getId()));
        });
    }
}

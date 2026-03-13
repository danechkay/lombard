package ru.lombard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.lombard.service.CartService;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.ProductService;

import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;
    private final ProductService productService;

    @GetMapping
    public String cart(Model model) {
        Long userId = currentUserService.getCurrentUserIdOrNull();
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("cartItems", cartService.getCart(userId));
        model.addAttribute("cartCount", cartService.getCartCount(userId));
        model.addAttribute("cartTotal", cartService.getCartTotal(userId));
        return "cart";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity,
                      RedirectAttributes redirectAttributes) {
        Long userId = currentUserService.getCurrentUserIdOrNull();
        if (userId == null) {
            redirectAttributes.addFlashAttribute("message", "Войдите, чтобы добавить товар в корзину.");
            return "redirect:/login";
        }
        try {
            cartService.addToCart(userId, productId, quantity);
            redirectAttributes.addFlashAttribute("message", "Товар добавлен в корзину.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String update(@RequestParam Long cartItemId, @RequestParam int quantity) {
        Long userId = currentUserService.getCurrentUserIdOrNull();
        if (userId == null) return "redirect:/login";
        cartService.updateQuantity(userId, cartItemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long productId) {
        Long userId = currentUserService.getCurrentUserIdOrNull();
        if (userId == null) return "redirect:/login";
        cartService.removeFromCart(userId, productId);
        return "redirect:/cart";
    }
}

package ru.lombard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.lombard.entity.Order;
import ru.lombard.entity.User;
import ru.lombard.service.CartService;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;
    private final CartService cartService;

    @GetMapping("/checkout")
    public String checkout(Model model) {
        User user = currentUserService.getCurrentUser().orElse(null);
        if (user == null) return "redirect:/login";
        List<?> cartItems = cartService.getCart(user.getId());
        if (cartItems.isEmpty()) {
            return "redirect:/catalog";
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("user", user);
        return "order-checkout";
    }

    @PostMapping("/place")
    public String placeOrder(@RequestParam(required = false) String comment, RedirectAttributes redirectAttributes) {
        User user = currentUserService.getCurrentUser().orElse(null);
        if (user == null) return "redirect:/login";
        try {
            Order order = orderService.createOrder(user, comment);
            redirectAttributes.addFlashAttribute("message", "Заказ №" + order.getId() + " оформлен. Ожидайте звонка оператора.");
            return "redirect:/account/orders";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }
}

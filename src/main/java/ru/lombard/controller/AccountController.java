package ru.lombard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.lombard.dto.OrderDto;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final CurrentUserService currentUserService;
    private final OrderService orderService;

    @GetMapping
    public String account(Model model) {
        var user = currentUserService.getCurrentUser().orElse(null);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "account";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        var user = currentUserService.getCurrentUser().orElse(null);
        if (user == null) return "redirect:/login";
        List<OrderDto> orders = orderService.findOrdersByUser(user.getId(), 50);
        model.addAttribute("orders", orders);
        model.addAttribute("user", user);
        return "account-orders";
    }
}

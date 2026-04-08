package ru.lombard.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.lombard.entity.Order;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.OrderService;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class AdminOrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        var actor = currentUserService.getCurrentUser().orElseThrow();
        var orders = orderService.findAllOrders(actor, page, 20);
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        var actor = currentUserService.getCurrentUser().orElseThrow();
        model.addAttribute("order", orderService.findDtoByIdForActor(actor, id));
        return "admin/order-view";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status, RedirectAttributes ra) {
        var actor = currentUserService.getCurrentUser().orElseThrow();
        orderService.updateStatusForActor(actor, id, Order.OrderStatus.valueOf(status));
        ra.addFlashAttribute("message", "Статус заказа обновлён.");
        return "redirect:/admin/orders/" + id;
    }
}

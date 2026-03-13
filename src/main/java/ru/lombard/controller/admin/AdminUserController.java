package ru.lombard.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.lombard.entity.User;
import ru.lombard.repository.UserRepository;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    public String list(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/{id}/block")
    public String block(@PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findById(id).orElseThrow();
        user.setBlocked(true);
        userRepository.save(user);
        ra.addFlashAttribute("message", "Пользователь заблокирован.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/unblock")
    public String unblock(@PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findById(id).orElseThrow();
        user.setBlocked(false);
        userRepository.save(user);
        ra.addFlashAttribute("message", "Пользователь разблокирован.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam String role, RedirectAttributes ra) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(User.Role.valueOf(role));
        userRepository.save(user);
        ra.addFlashAttribute("message", "Роль обновлена.");
        return "redirect:/admin/users";
    }
}

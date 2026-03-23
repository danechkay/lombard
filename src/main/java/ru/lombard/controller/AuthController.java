package ru.lombard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.lombard.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.register(form.getEmail(), form.getPassword(), form.getFullName(), form.getPhone());
            redirectAttributes.addFlashAttribute("message", "Регистрация успешна. Войдите в систему.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "email.exists", e.getMessage());
            return "register";
        } catch (DataIntegrityViolationException e) {
            result.reject("register.error", "Не удалось завершить регистрацию. Проверьте корректность данных.");
            return "register";
        }
    }

    @Data
    public static class RegisterForm {
        @NotBlank(message = "Укажите email")
        @Email
        private String email;
        @NotBlank(message = "Укажите пароль")
        @Size(min = 5, message = "Пароль не менее 5 символов")
        private String password;
        @NotBlank(message = "Укажите имя")
        private String fullName;
        @Size(max = 20, message = "Телефон не должен быть длиннее 20 символов")
        private String phone;
    }
}

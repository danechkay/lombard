package ru.lombard.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.lombard.entity.User;
import ru.lombard.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) return;
        User admin = User.builder()
                .email("admin@lombard.ru")
                .passwordHash(passwordEncoder.encode("password"))
                .fullName("Администратор")
                .role(User.Role.ADMIN)
                .blocked(false)
                .build();
        userRepository.save(admin);
        User manager = User.builder()
                .email("manager@lombard.ru")
                .passwordHash(passwordEncoder.encode("password"))
                .fullName("Менеджер")
                .role(User.Role.MANAGER)
                .blocked(false)
                .build();
        userRepository.save(manager);
    }
}

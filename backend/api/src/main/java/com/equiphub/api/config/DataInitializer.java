package com.equiphub.api.config;
import com.equiphub.api.model.User;
import com.equiphub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;    


@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@equiphub.test")) {
            User admin = User.builder()
                .email("admin@equiphub.test")
                .passwordHash(passwordEncoder.encode("Admin@1234"))
                .firstName("System")
                .lastName("Admin")
                .role(User.Role.SYSTEMADMIN)
                .status(User.Status.ACTIVE)
                .emailVerified(true)
                .build();
            userRepository.save(admin);
            log.info("✅ Default admin created: admin@equiphub.test / Admin@1234");
        }
    }
}
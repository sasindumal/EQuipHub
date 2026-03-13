package com.equiphub.api.config;

import com.equiphub.api.model.Department;
import com.equiphub.api.model.User;
import com.equiphub.api.repository.DepartmentRepository;
import com.equiphub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DataSeeder — runs on startup when the 'dev' or 'demo' Spring profile is active.
 * Creates two departments (CSE, EEE) and one test user per role with
 * pre-hashed passwords.  All demo passwords are "Demo@1234".
 *
 * Usage:
 *   application.properties  →  spring.profiles.active=dev
 *   OR pass JVM arg          →  -Dspring.profiles.active=demo
 */
@Slf4j
@Component
@Profile({"dev", "demo"})
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository       userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder      passwordEncoder;

    // -----------------------------------------------------------------------
    // Demo password used for every seeded account
    // -----------------------------------------------------------------------
    private static final String DEMO_PASSWORD = "Demo@1234";

    @Override
    @Transactional
    public void run(String... args) {
        log.info("╔══════════════════════════════════════════╗");
        log.info("║   EQuipHub DataSeeder — demo/dev mode    ║");
        log.info("╚══════════════════════════════════════════╝");

        // ── 1. Departments ───────────────────────────────────────────────
        Department cse = seedDepartment("CSE", "Department of Computer Engineering",
                "Computer Science and Engineering department");
        Department eee = seedDepartment("EEE", "Department of Electrical & Electronics Engineering",
                "Electrical and Electronics Engineering department");

        // ── 2. System-level users (no department) ────────────────────────
        seedUser("admin@equiphub.lk",          "System",      "Admin",
                 null, User.Role.SYSTEMADMIN,       null, null);

        // ── 3. CSE department users ───────────────────────────────────────
        seedUser("hod.cse@equiphub.lk",        "Amal",        "Perera",
                 cse,  User.Role.HEADOFDEPARTMENT,  null, null);

        seedUser("deptadmin.cse@equiphub.lk",  "Nimal",       "Silva",
                 cse,  User.Role.DEPARTMENTADMIN,   null, null);

        seedUser("lecturer1.cse@equiphub.lk",  "Saman",       "Jayawardena",
                 cse,  User.Role.LECTURER,          null, null);

        seedUser("lecturer2.cse@equiphub.lk",  "Kumari",      "Fernando",
                 cse,  User.Role.LECTURER,          null, null);

        seedUser("aptlecturer.cse@equiphub.lk","Ruwan",       "Bandara",
                 cse,  User.Role.APPOINTEDLECTURER, null, null);

        seedUser("instructor.cse@equiphub.lk", "Thilina",     "Rajapaksa",
                 cse,  User.Role.INSTRUCTOR,        null, null);

        seedUser("to.cse@equiphub.lk",         "Pradeep",     "Gunawardena",
                 cse,  User.Role.TECHNICALOFFICER,  null, null);

        seedUser("student1.cse@equiphub.lk",   "Kasun",       "Madushanka",
                 cse,  User.Role.STUDENT,           "220001C", 2);

        seedUser("student2.cse@equiphub.lk",   "Dilini",      "Wickramasinghe",
                 cse,  User.Role.STUDENT,           "220002C", 2);

        seedUser("student3.cse@equiphub.lk",   "Hasini",      "Karunarathna",
                 cse,  User.Role.STUDENT,           "210003C", 3);

        // ── 4. EEE department users ───────────────────────────────────────
        seedUser("hod.eee@equiphub.lk",        "Chaminda",    "Dissanayake",
                 eee,  User.Role.HEADOFDEPARTMENT,  null, null);

        seedUser("deptadmin.eee@equiphub.lk",  "Sunethra",    "Rathnayake",
                 eee,  User.Role.DEPARTMENTADMIN,   null, null);

        seedUser("lecturer.eee@equiphub.lk",   "Buddhika",    "Samarasinghe",
                 eee,  User.Role.LECTURER,          null, null);

        seedUser("instructor.eee@equiphub.lk", "Chatura",     "Liyanage",
                 eee,  User.Role.INSTRUCTOR,        null, null);

        seedUser("to.eee@equiphub.lk",         "Manjula",     "Weerasinghe",
                 eee,  User.Role.TECHNICALOFFICER,  null, null);

        seedUser("student1.eee@equiphub.lk",   "Ishara",      "Pathirana",
                 eee,  User.Role.STUDENT,           "220001E", 2);

        seedUser("student2.eee@equiphub.lk",   "Lasith",      "Madushan",
                 eee,  User.Role.STUDENT,           "210002E", 3);

        log.info("✅  DataSeeder complete — {} users in database",
                userRepository.count());
        log.info("🔑  Demo password for all accounts: {}", DEMO_PASSWORD);
        printUserTable();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Department seedDepartment(String code, String name, String description) {
        return departmentRepository.findByCode(code).orElseGet(() -> {
            Department dept = Department.builder()
                    .code(code)
                    .name(name)
                    .description(description)
                    .isActive(true)
                    .build();
            Department saved = departmentRepository.save(dept);
            log.info("  🏛  Created department: {} — {}", code, name);
            return saved;
        });
    }

    private void seedUser(String email, String firstName, String lastName,
                          Department department, User.Role role,
                          String indexNumber, Integer semesterYear) {
        if (userRepository.existsByEmail(email)) {
            log.debug("  ⏩  User already exists, skipping: {}", email);
            return;
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .department(department)
                .indexNumber(indexNumber)
                .semesterYear(semesterYear)
                .status(User.Status.ACTIVE)
                .emailVerified(Boolean.TRUE)   // skip email verification for demo users
                .lastLogin(LocalDateTime.now())
                .build();

        userRepository.save(user);
        log.info("  👤  [{}] {}  {} — {}", role, firstName, lastName, email);
    }

    private void printUserTable() {
        log.info("");
        log.info("╔══════════════════╦════════════════════════════════════╦══════════════════╗");
        log.info("║ Role             ║ Email                              ║ Password         ║");
        log.info("╠══════════════════╬════════════════════════════════════╬══════════════════╣");
        List.of(
            "SYSTEMADMIN       | admin@equiphub.lk                  ",
            "HOD (CSE)         | hod.cse@equiphub.lk                ",
            "DEPTADMIN (CSE)   | deptadmin.cse@equiphub.lk          ",
            "LECTURER (CSE)    | lecturer1.cse@equiphub.lk          ",
            "LECTURER (CSE)    | lecturer2.cse@equiphub.lk          ",
            "APT.LECTURER(CSE) | aptlecturer.cse@equiphub.lk        ",
            "INSTRUCTOR (CSE)  | instructor.cse@equiphub.lk         ",
            "T.OFFICER (CSE)   | to.cse@equiphub.lk                 ",
            "STUDENT (CSE)     | student1.cse@equiphub.lk           ",
            "STUDENT (CSE)     | student2.cse@equiphub.lk           ",
            "STUDENT (CSE)     | student3.cse@equiphub.lk           ",
            "HOD (EEE)         | hod.eee@equiphub.lk                ",
            "DEPTADMIN (EEE)   | deptadmin.eee@equiphub.lk          ",
            "LECTURER (EEE)    | lecturer.eee@equiphub.lk           ",
            "INSTRUCTOR (EEE)  | instructor.eee@equiphub.lk         ",
            "T.OFFICER (EEE)   | to.eee@equiphub.lk                 ",
            "STUDENT (EEE)     | student1.eee@equiphub.lk           ",
            "STUDENT (EEE)     | student2.eee@equiphub.lk           "
        ).forEach(row -> log.info("║ {} | Demo@1234        ║", row));
        log.info("╚══════════════════╩════════════════════════════════════╩══════════════════╝");
    }
}

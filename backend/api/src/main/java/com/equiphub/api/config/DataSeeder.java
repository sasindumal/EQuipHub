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
 *
 * Email conventions:
 *   STUDENT  → 20YYX000@eng.jfn.ac.lk  (matches AuthController regex: ^20\d{2}[A-Za-z]\d{3}@eng\.jfn\.ac\.lk$)
 *   STAFF    → role.dept@eng.jfn.ac.lk  (institutional format, no regex restriction at login)
 *
 * All demo accounts use password: Demo@1234
 *
 * Activate with:
 *   spring.profiles.active=dev   (in application.properties)
 *   OR  -Dspring.profiles.active=demo  (JVM arg)
 */
@Slf4j
@Component
@Profile({"dev", "demo"})
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository       userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder      passwordEncoder;

    private static final String DEMO_PASSWORD = "Demo@1234";

    @Override
    @Transactional
    public void run(String... args) {
        log.info("╔══════════════════════════════════════════╗");
        log.info("║   EQuipHub DataSeeder — demo/dev mode    ║");
        log.info("╚══════════════════════════════════════════╝");

        // ── 1. Departments ────────────────────────────────────────────────────
        Department cse = seedDepartment(
                "CSE",
                "Department of Computer Engineering",
                "Computer Science and Engineering department");

        Department eee = seedDepartment(
                "EEE",
                "Department of Electrical & Electronics Engineering",
                "Electrical and Electronics Engineering department");

        // ── 2. System Admin (no department) ──────────────────────────────────
        seedUser("sysadmin@eng.jfn.ac.lk",
                 "System", "Admin",
                 null, User.Role.SYSTEMADMIN, null, null);

        // ── 3. CSE Department Users ───────────────────────────────────────────

        seedUser("hod.cse@eng.jfn.ac.lk",
                 "Amal", "Perera",
                 cse, User.Role.HEADOFDEPARTMENT, null, null);

        seedUser("deptadmin.cse@eng.jfn.ac.lk",
                 "Nimal", "Silva",
                 cse, User.Role.DEPARTMENTADMIN, null, null);

        seedUser("saman.lecturer@eng.jfn.ac.lk",
                 "Saman", "Jayawardena",
                 cse, User.Role.LECTURER, null, null);

        seedUser("kumari.lecturer@eng.jfn.ac.lk",
                 "Kumari", "Fernando",
                 cse, User.Role.LECTURER, null, null);

        seedUser("ruwan.aptlecturer@eng.jfn.ac.lk",
                 "Ruwan", "Bandara",
                 cse, User.Role.APPOINTEDLECTURER, null, null);

        seedUser("thilina.instructor@eng.jfn.ac.lk",
                 "Thilina", "Rajapaksa",
                 cse, User.Role.INSTRUCTOR, null, null);

        seedUser("pradeep.to@eng.jfn.ac.lk",
                 "Pradeep", "Gunawardena",
                 cse, User.Role.TECHNICALOFFICER, null, null);

        // Students — CSE  (regex: ^20\d{2}[A-Za-z]\d{3}@eng\.jfn\.ac\.lk$)
        seedUser("2022E001@eng.jfn.ac.lk",
                 "Kasun", "Madushanka",
                 cse, User.Role.STUDENT, "2022E001", 2);

        seedUser("2022E002@eng.jfn.ac.lk",
                 "Dilini", "Wickramasinghe",
                 cse, User.Role.STUDENT, "2022E002", 2);

        seedUser("2021E003@eng.jfn.ac.lk",
                 "Hasini", "Karunarathna",
                 cse, User.Role.STUDENT, "2021E003", 3);

        // ── 4. EEE Department Users ───────────────────────────────────────────

        seedUser("hod.eee@eng.jfn.ac.lk",
                 "Chaminda", "Dissanayake",
                 eee, User.Role.HEADOFDEPARTMENT, null, null);

        seedUser("deptadmin.eee@eng.jfn.ac.lk",
                 "Sunethra", "Rathnayake",
                 eee, User.Role.DEPARTMENTADMIN, null, null);

        seedUser("buddhika.lecturer@eng.jfn.ac.lk",
                 "Buddhika", "Samarasinghe",
                 eee, User.Role.LECTURER, null, null);

        seedUser("chatura.instructor@eng.jfn.ac.lk",
                 "Chatura", "Liyanage",
                 eee, User.Role.INSTRUCTOR, null, null);

        seedUser("manjula.to@eng.jfn.ac.lk",
                 "Manjula", "Weerasinghe",
                 eee, User.Role.TECHNICALOFFICER, null, null);

        // Students — EEE
        seedUser("2022E101@eng.jfn.ac.lk",
                 "Ishara", "Pathirana",
                 eee, User.Role.STUDENT, "2022E101", 2);

        seedUser("2021E102@eng.jfn.ac.lk",
                 "Lasith", "Madushan",
                 eee, User.Role.STUDENT, "2021E102", 3);

        log.info("✅  DataSeeder complete — {} users in database", userRepository.count());
        log.info("🔑  Demo password for ALL accounts: {}", DEMO_PASSWORD);
        printUserTable();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Department seedDepartment(String code, String name, String description) {
        return departmentRepository.findByCode(code).orElseGet(() -> {
            Department dept = Department.builder()
                    .code(code)
                    .name(name)
                    .description(description)
                    .isActive(true)
                    .build();
            Department saved = departmentRepository.save(dept);
            log.info("  🏛  Created department: [{}] {}", code, name);
            return saved;
        });
    }

    /**
     * Creates a user only if the email does not already exist (idempotent).
     * emailVerified = TRUE so demo accounts skip the email-verification flow.
     */
    private void seedUser(String email,
                          String firstName,
                          String lastName,
                          Department department,
                          User.Role role,
                          String indexNumber,
                          Integer semesterYear) {

        if (userRepository.existsByEmail(email)) {
            log.debug("  ⏩  Skipping existing user: {}", email);
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
                .emailVerified(Boolean.TRUE)   // bypass email-verification for demo
                .lastLogin(LocalDateTime.now())
                .build();

        userRepository.save(user);
        log.info("  👤  [{}] {} {} → {}", role, firstName, lastName, email);
    }

    private void printUserTable() {
        log.info("");
        log.info("╔══════════════════════╦══════════════════════════════════════╦═════════════╗");
        log.info("║ Role                 ║ Email (username)                     ║ Password    ║");
        log.info("╠══════════════════════╬══════════════════════════════════════╬═════════════╣");
        List.of(
            new String[]{"SYSTEMADMIN          ", "sysadmin@eng.jfn.ac.lk              "},
            new String[]{"HOD          [CSE]   ", "hod.cse@eng.jfn.ac.lk               "},
            new String[]{"DEPT ADMIN   [CSE]   ", "deptadmin.cse@eng.jfn.ac.lk         "},
            new String[]{"LECTURER     [CSE]   ", "saman.lecturer@eng.jfn.ac.lk        "},
            new String[]{"LECTURER     [CSE]   ", "kumari.lecturer@eng.jfn.ac.lk       "},
            new String[]{"APT LECTURER [CSE]   ", "ruwan.aptlecturer@eng.jfn.ac.lk     "},
            new String[]{"INSTRUCTOR   [CSE]   ", "thilina.instructor@eng.jfn.ac.lk    "},
            new String[]{"TECH OFFICER [CSE]   ", "pradeep.to@eng.jfn.ac.lk            "},
            new String[]{"STUDENT      [CSE]   ", "2022E001@eng.jfn.ac.lk              "},
            new String[]{"STUDENT      [CSE]   ", "2022E002@eng.jfn.ac.lk              "},
            new String[]{"STUDENT      [CSE]   ", "2021E003@eng.jfn.ac.lk              "},
            new String[]{"HOD          [EEE]   ", "hod.eee@eng.jfn.ac.lk               "},
            new String[]{"DEPT ADMIN   [EEE]   ", "deptadmin.eee@eng.jfn.ac.lk         "},
            new String[]{"LECTURER     [EEE]   ", "buddhika.lecturer@eng.jfn.ac.lk     "},
            new String[]{"INSTRUCTOR   [EEE]   ", "chatura.instructor@eng.jfn.ac.lk    "},
            new String[]{"TECH OFFICER [EEE]   ", "manjula.to@eng.jfn.ac.lk            "},
            new String[]{"STUDENT      [EEE]   ", "2022E101@eng.jfn.ac.lk              "},
            new String[]{"STUDENT      [EEE]   ", "2021E102@eng.jfn.ac.lk              "}
        ).forEach(row -> log.info("║ {} ║ {} ║ Demo@1234   ║", row[0], row[1]));
        log.info("╚══════════════════════╩══════════════════════════════════════╩═════════════╝");
    }
}

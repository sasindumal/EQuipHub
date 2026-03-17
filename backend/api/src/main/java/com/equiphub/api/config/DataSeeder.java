package com.equiphub.api.config;

import com.equiphub.api.model.Department;
import com.equiphub.api.model.Equipment;
import com.equiphub.api.model.EquipmentCategory;
import com.equiphub.api.model.User;
import com.equiphub.api.repository.DepartmentRepository;
import com.equiphub.api.repository.EquipmentCategoryRepository;
import com.equiphub.api.repository.EquipmentRepository;
import com.equiphub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@Profile({"dev", "demo"})
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EquipmentCategoryRepository equipmentCategoryRepository;
    private final EquipmentRepository equipmentRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEMO_PASSWORD = "Demo@1234";
    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        log.info("╔══════════════════════════════════════════╗");
        log.info("║   EQuipHub DataSeeder — demo/dev mode    ║");
        log.info("╚══════════════════════════════════════════╝");

        Department cse = seedDepartment("CSE", "Department of Computer Engineering", "Computer Science and Engineering department");
        Department eee = seedDepartment("EEE", "Department of Electrical & Electronics Engineering", "Electrical and Electronics Engineering department");

        seedSystemAdmin();
        seedCseUsers(cse);
        seedEeeUsers(eee);

        seedEquipmentCategories();
        seedEquipment(cse, eee);

        log.info("✅  DataSeeder complete");
        log.info("   - Users: {}", userRepository.count());
        log.info("   - Equipment: {}", equipmentRepository.count());
        log.info("🔑  Demo password for ALL accounts: {}", DEMO_PASSWORD);
        printUserTable();
    }

    private void seedSystemAdmin() {
        if (!userRepository.existsByEmail("sysadmin@eng.jfn.ac.lk")) {
            User admin = User.builder()
                    .email("sysadmin@eng.jfn.ac.lk")
                    .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                    .firstName("System")
                    .lastName("Admin")
                    .role(User.Role.SYSTEMADMIN)
                    .status(User.Status.ACTIVE)
                    .emailVerified(true)
                    .build();
            userRepository.save(admin);
            log.info("  👤  [SYSTEMADMIN] System Admin → sysadmin@eng.jfn.ac.lk");
        }
    }

    private void seedCseUsers(Department cse) {
        seedUser("hod.cse@eng.jfn.ac.lk", "Amal", "Perera", cse, User.Role.HEADOFDEPARTMENT, null, null);
        seedUser("deptadmin.cse@eng.jfn.ac.lk", "Nimal", "Silva", cse, User.Role.DEPARTMENTADMIN, null, null);
        seedUser("saman.lecturer@eng.jfn.ac.lk", "Saman", "Jayawardena", cse, User.Role.LECTURER, null, null);
        seedUser("kumari.lecturer@eng.jfn.ac.lk", "Kumari", "Fernando", cse, User.Role.LECTURER, null, null);
        seedUser("ruwan.aptlecturer@eng.jfn.ac.lk", "Ruwan", "Bandara", cse, User.Role.APPOINTEDLECTURER, null, null);
        seedUser("thilina.instructor@eng.jfn.ac.lk", "Thilina", "Rajapaksa", cse, User.Role.INSTRUCTOR, null, null);
        seedUser("pradeep.to@eng.jfn.ac.lk", "Pradeep", "Gunawardena", cse, User.Role.TECHNICALOFFICER, null, null);
        seedUser("2022E001@eng.jfn.ac.lk", "Kasun", "Madushanka", cse, User.Role.STUDENT, "2022E001", 2);
        seedUser("2022E002@eng.jfn.ac.lk", "Dilini", "Wickramasinghe", cse, User.Role.STUDENT, "2022E002", 2);
        seedUser("2021E003@eng.jfn.ac.lk", "Hasini", "Karunarathna", cse, User.Role.STUDENT, "2021E003", 3);
    }

    private void seedEeeUsers(Department eee) {
        seedUser("hod.eee@eng.jfn.ac.lk", "Chaminda", "Dissanayake", eee, User.Role.HEADOFDEPARTMENT, null, null);
        seedUser("deptadmin.eee@eng.jfn.ac.lk", "Sunethra", "Rathnayake", eee, User.Role.DEPARTMENTADMIN, null, null);
        seedUser("buddhika.lecturer@eng.jfn.ac.lk", "Buddhika", "Samarasinghe", eee, User.Role.LECTURER, null, null);
        seedUser("chatura.instructor@eng.jfn.ac.lk", "Chatura", "Liyanage", eee, User.Role.INSTRUCTOR, null, null);
        seedUser("manjula.to@eng.jfn.ac.lk", "Manjula", "Weerasinghe", eee, User.Role.TECHNICALOFFICER, null, null);
        seedUser("2022E101@eng.jfn.ac.lk", "Ishara", "Pathirana", eee, User.Role.STUDENT, "2022E101", 2);
        seedUser("2021E102@eng.jfn.ac.lk", "Lasith", "Madushan", eee, User.Role.STUDENT, "2021E102", 3);
    }

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

    private void seedUser(String email, String firstName, String lastName, Department department, User.Role role, String indexNumber, Integer semesterYear) {
        if (userRepository.existsByEmail(email)) {
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
                .emailVerified(true)
                .lastLogin(LocalDateTime.now())
                .build();
        userRepository.save(user);
        log.info("  👤  [{}] {} {} → {}", role, firstName, lastName, email);
    }

    private void seedEquipmentCategories() {
        if (equipmentCategoryRepository.count() > 0) {
            log.info("  ⏩  Equipment categories already exist, skipping...");
            return;
        }

        List<EquipmentCategory> categories = new ArrayList<>();
        categories.add(EquipmentCategory.builder().name("MEASUREMENT").description("Measurement and testing equipment").damageMultiplierBase(1.2).typicalReplacementCost(BigDecimal.valueOf(50000)).build());
        categories.add(EquipmentCategory.builder().name("POWER_SUPPLY").description("Power supplies and electrical equipment").damageMultiplierBase(1.0).typicalReplacementCost(BigDecimal.valueOf(30000)).build());
        categories.add(EquipmentCategory.builder().name("DEVELOPMENT_TOOLS").description("Development boards and programmers").damageMultiplierBase(1.5).typicalReplacementCost(BigDecimal.valueOf(25000)).build());
        categories.add(EquipmentCategory.builder().name("COMPONENTS").description("Electronic components and parts").damageMultiplierBase(1.0).typicalReplacementCost(BigDecimal.valueOf(5000)).build());
        categories.add(EquipmentCategory.builder().name("SPECIALIZED").description("Specialized engineering equipment").damageMultiplierBase(1.3).typicalReplacementCost(BigDecimal.valueOf(100000)).build());
        categories.add(EquipmentCategory.builder().name("PORTABLE").description("Portable/borrowable equipment").damageMultiplierBase(1.4).typicalReplacementCost(BigDecimal.valueOf(15000)).build());

        equipmentCategoryRepository.saveAll(categories);
        log.info("  📦  Created {} equipment categories", categories.size());
    }

    private void seedEquipment(Department cse, Department eee) {
        if (equipmentRepository.count() > 0) {
            log.info("  ⏩  Equipment already exists, skipping...");
            return;
        }

        List<EquipmentCategory> categories = equipmentCategoryRepository.findAll();
        List<Equipment> equipmentList = new ArrayList<>();

        String[] cseLocations = {"Lab 101", "Lab 102", "Lab 103", "Lab 201", "Lab 202", "Workshop A", "Workshop B", "Store Room 1"};
        String[] eeeLocations = {"Electrical Lab 1", "Electrical Lab 2", "Power Lab", "Electronics Lab", "Machine Lab", "Control Lab", "High Voltage Lab"};

        String[] cseEquipment = {
            "Arduino Uno R3", "Arduino Mega 2560", "Arduino Nano", "Raspberry Pi 4B 8GB", "Raspberry Pi 3B+", "ESP32 DevKit", "ESP8266 NodeMCU", "STM32F103C8T6",
            "Digital Multimeter", "Oscilloscope 20MHz", "Oscilloscope 100MHz", "Function Generator", "DC Power Supply 30V/5A", "DC Power Supply 12V/10A",
            "Logic Analyzer", "Signal Generator", "Soldering Station", "Hot Air Rework Station", "Breadboard Kit", "Jumper Wire Kit", "Resistor Kit", "Capacitor Kit",
            "LED Display Module", "LCD 16x2", "OLED Display 0.96\"", "Touch Screen 2.8\"", "Servo Motor MG996R", "Stepper Motor 28BYJ-48", "DC Motor 12V",
            "Ultrasonic Sensor HC-SR04", "Temperature Sensor DS18B20", "IR Sensor Module", "Light Sensor LDR", "Pressure Sensor BMP280", "Accelerometer MPU6050",
            "GPS Module NEO-6M", "Bluetooth Module HC-05", "WiFi Module ESP01", "RFID Reader RC522", "Fingerprint Sensor", "Webcam Logitech C920",
            "3D Printer Ender 3", "Laser Cutter", "CNC Router", "Oscilloscope Probes", "BNC Cables", "USB Cable Kit", "Ethernet Cable Tester"
        };

        String[] eeeEquipment = {
            "Digital Multimeter Fluke", "Oscilloscope Tektronix", "Oscilloscope Rigol", "Function Generator GW Instek", "DC Power Supply Keysight", "AC Power Supply",
            "Isolation Transformer", "Variac 250VA", "Wheatstone Bridge", "Kelvin Bridge", "Potentiometer Decade Box", "Capacitor Decade Box", "Inductance Meter",
            "LCR Meter", "Earth Resistance Tester", "Insulation Tester Megger", "High Voltage Probe", "Current Probe", "Power Analyzer", "Energy Meter",
            "Solar Panel 100W", "Solar Charge Controller", "Inverter 500W", "Battery Charger", "Lead Acid Battery 12V", "Li-ion Battery Pack",
            "PLC Siemens S7-1200", "PLC Mitsubishi FX", "PLC Arduino Compatible", "HMI Touch Panel", "Relay Module 8-Channel", "Contactor 40A", "MCB 10A",
            "Circuit Breaker", "Fuse Kit", "Terminal Block Kit", "DIN Rail", "Cable Tray", "Multicore Cable", "Flexible Cable", "Shielded Cable",
            "Motor Starter", "Motor Driver L298N", "Motor Driver BTS7960", "Servo Drive", "VFD Variable Frequency Drive", "PID Controller", "Temperature Controller",
            "Thermocouple Type K", "RTD Sensor", "Pressure Transducer", "Flow Meter", "Level Sensor", "pH Meter", "Conductivity Meter"
        };

        Equipment.EquipmentStatus[] statuses = {
            Equipment.EquipmentStatus.AVAILABLE,
            Equipment.EquipmentStatus.AVAILABLE,
            Equipment.EquipmentStatus.AVAILABLE,
            Equipment.EquipmentStatus.AVAILABLE,
            Equipment.EquipmentStatus.RESERVED,
            Equipment.EquipmentStatus.INUSE,
            Equipment.EquipmentStatus.MAINTENANCE,
            Equipment.EquipmentStatus.DAMAGED
        };

        for (int i = 0; i < 500; i++) {
            Equipment eq = Equipment.builder()
                    .name(cseEquipment[i % cseEquipment.length] + (i / cseEquipment.length > 0 ? " (" + (i / cseEquipment.length + 1) + ")" : ""))
                    .category(categories.get(i % categories.size()))
                    .type(i % 3 == 0 ? Equipment.EquipmentType.BORROWABLE : Equipment.EquipmentType.LABDEDICATED)
                    .department(cse)
                    .description("Equipment for Computer Engineering department labs")
                    .status(statuses[random.nextInt(statuses.length)])
                    .totalQuantity(random.nextInt(10) + 1)
                    .currentLocation(cseLocations[random.nextInt(cseLocations.length)])
                    .currentCondition(random.nextInt(11))
                    .purchaseDate(LocalDate.now().minusDays(random.nextInt(3650)))
                    .purchaseValue(BigDecimal.valueOf(random.nextDouble() * 100000 + 1000))
                    .replacementCost(BigDecimal.valueOf(random.nextDouble() * 120000 + 1500))
                    .serialNumber("CSE-" + String.format("%05d", i + 1))
                    .maintenanceIntervalDays(90 + random.nextInt(180))
                    .lastMaintenanceDate(LocalDate.now().minusDays(random.nextInt(180)))
                    .nextMaintenanceDate(LocalDate.now().plusDays(random.nextInt(180)))
                    .retired(false)
                    .build();
            equipmentList.add(eq);
        }

        for (int i = 0; i < 500; i++) {
            Equipment eq = Equipment.builder()
                    .name(eeeEquipment[i % eeeEquipment.length] + (i / eeeEquipment.length > 0 ? " (" + (i / eeeEquipment.length + 1) + ")" : ""))
                    .category(categories.get(i % categories.size()))
                    .type(i % 4 == 0 ? Equipment.EquipmentType.BORROWABLE : Equipment.EquipmentType.LABDEDICATED)
                    .department(eee)
                    .description("Equipment for Electrical & Electronics Engineering department labs")
                    .status(statuses[random.nextInt(statuses.length)])
                    .totalQuantity(random.nextInt(8) + 1)
                    .currentLocation(eeeLocations[random.nextInt(eeeLocations.length)])
                    .currentCondition(random.nextInt(11))
                    .purchaseDate(LocalDate.now().minusDays(random.nextInt(3650)))
                    .purchaseValue(BigDecimal.valueOf(random.nextDouble() * 150000 + 2000))
                    .replacementCost(BigDecimal.valueOf(random.nextDouble() * 180000 + 2500))
                    .serialNumber("EEE-" + String.format("%05d", i + 1))
                    .maintenanceIntervalDays(60 + random.nextInt(150))
                    .lastMaintenanceDate(LocalDate.now().minusDays(random.nextInt(180)))
                    .nextMaintenanceDate(LocalDate.now().plusDays(random.nextInt(180)))
                    .retired(false)
                    .build();
            equipmentList.add(eq);
        }

        equipmentRepository.saveAll(equipmentList);
        log.info("  🔧  Seeded {} equipment items (500 CSE, 500 EEE)", equipmentList.size());
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

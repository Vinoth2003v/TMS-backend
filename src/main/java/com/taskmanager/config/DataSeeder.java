package com.taskmanager.config;

import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final com.taskmanager.repository.CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, 
                      com.taskmanager.repository.CategoryRepository categoryRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private User makeUser(String name, String email, String rawPassword, User.Role role, String dept) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setDepartment(dept);
        return u;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(makeUser("Admin",       "admin@gmail.com",   "admin",   User.Role.ADMIN,           "Administration"));
            userRepository.save(makeUser("Manager",     "manager@gmail.com", "manager", User.Role.MANAGER,         "Engineering"));
            userRepository.save(makeUser("Team Member", "member@gmail.com",  "member",  User.Role.TEAM_MEMBER,     "Engineering"));
            userRepository.save(makeUser("Vinoth",      "user@gmail.com",    "user",    User.Role.INDIVIDUAL_USER, "Development"));
            System.out.println("✅ Default users seeded successfully!");
        }

        if (categoryRepository.count() == 0) {
            categoryRepository.save(new com.taskmanager.entity.Category("Development", "Software engineering and development tasks"));
            categoryRepository.save(new com.taskmanager.entity.Category("Design", "UI/UX design and creative work"));
            categoryRepository.save(new com.taskmanager.entity.Category("Marketing", "Marketing, campaigns, and user growth"));
            categoryRepository.save(new com.taskmanager.entity.Category("Operations", "DevOps, infrastructure, and administration"));
            categoryRepository.save(new com.taskmanager.entity.Category("Research", "Analysis, discovery, and research tasks"));
            categoryRepository.save(new com.taskmanager.entity.Category("Testing / QA", "Quality assurance and test automation"));
            System.out.println("✅ Default categories seeded successfully!");
        }
    }
}

package com.bank.config;

import com.bank.entity.User;
import com.bank.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Creates a default admin user on first start: admin@bank.com / Admin@123 */
@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByEmail("admin@bank.com")) {
                users.save(new User("Bank Admin", "admin@bank.com", encoder.encode("Admin@123"), "ADMIN"));
                System.out.println(">>> Default admin created: admin@bank.com / Admin@123");
            }
        };
    }
}

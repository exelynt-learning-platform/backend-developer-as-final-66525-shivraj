package com.resource.bookingsystem.seed;

import com.resource.bookingsystem.entity.Resource;
import com.resource.bookingsystem.entity.ResourceType;
import com.resource.bookingsystem.entity.Role;
import com.resource.bookingsystem.entity.User;
import com.resource.bookingsystem.repository.ResourceRepository;
import com.resource.bookingsystem.repository.UserRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, ResourceRepository resourceRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User("admin@resourcebooking.local", passwordEncoder.encode("admin123"), Role.ADMIN);
            User user = new User("user@resourcebooking.local", passwordEncoder.encode("user123"), Role.USER);
            userRepository.save(admin);
            userRepository.save(user);
        }

        if (resourceRepository.count() == 0) {
            resourceRepository.save(new Resource("Conference Room A", ResourceType.ROOM, "Floor 2", new BigDecimal("40.00"), true));
            resourceRepository.save(new Resource("BMW X5", ResourceType.VEHICLE, "Parking Lot 1", new BigDecimal("90.00"), true));
            resourceRepository.save(new Resource("Projector", ResourceType.EQUIPMENT, "IT Store", new BigDecimal("25.00"), true));
        }
    }
}

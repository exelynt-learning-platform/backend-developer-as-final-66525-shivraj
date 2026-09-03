package com.resource.bookingsystem.seed;

import com.resource.bookingsystem.entity.Reservation;
import com.resource.bookingsystem.entity.ReservationStatus;
import com.resource.bookingsystem.entity.Resource;
import com.resource.bookingsystem.entity.ResourceType;
import com.resource.bookingsystem.entity.Role;
import com.resource.bookingsystem.entity.User;
import com.resource.bookingsystem.repository.ReservationRepository;
import com.resource.bookingsystem.repository.ResourceRepository;
import com.resource.bookingsystem.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      ResourceRepository resourceRepository,
                      ReservationRepository reservationRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User admin = null;
        User user = null;

        if (userRepository.count() == 0) {
            admin = userRepository.save(new User("admin@resourcebooking.local", passwordEncoder.encode("admin123"), Role.ADMIN));
            user = userRepository.save(new User("user@resourcebooking.local", passwordEncoder.encode("user123"), Role.USER));
        } else {
            admin = userRepository.findByEmail("admin@resourcebooking.local").orElse(null);
            user = userRepository.findByEmail("user@resourcebooking.local").orElse(null);
        }

        Resource room = null;
        Resource vehicle = null;
        Resource projector = null;

        if (resourceRepository.count() == 0) {
            room = resourceRepository.save(new Resource("Conference Room A", ResourceType.ROOM, "Floor 2", new BigDecimal("40.00"), true));
            vehicle = resourceRepository.save(new Resource("BMW X5", ResourceType.VEHICLE, "Parking Lot 1", new BigDecimal("90.00"), true));
            projector = resourceRepository.save(new Resource("Projector", ResourceType.EQUIPMENT, "IT Store", new BigDecimal("25.00"), true));
        }

        if (reservationRepository.count() == 0 && user != null && room != null) {
            LocalDateTime now = LocalDateTime.now();
            reservationRepository.save(new Reservation(
                room,
                user,
                ReservationStatus.CONFIRMED,
                new BigDecimal("80.00"),
                now.plusDays(1).withHour(10).withMinute(0).withSecond(0),
                now.plusDays(1).withHour(12).withMinute(0).withSecond(0)
            ));

            if (vehicle != null) {
                reservationRepository.save(new Reservation(
                    vehicle,
                    user,
                    ReservationStatus.PENDING,
                    new BigDecimal("180.00"),
                    now.plusDays(2).withHour(9).withMinute(0).withSecond(0),
                    now.plusDays(2).withHour(11).withMinute(0).withSecond(0)
                ));
            }
        }
    }
}

package com.rotacerta.api.security;

import com.rotacerta.api.model.AppUser;
import com.rotacerta.api.model.UserRole;
import com.rotacerta.api.repository.AppUserRepository;
import com.rotacerta.api.repository.CustomerRepository;
import com.rotacerta.api.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoUserInitializer implements ApplicationRunner {

    private final AppUserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;

    public DemoUserInitializer(
            AppUserRepository userRepository,
            CustomerRepository customerRepository,
            DriverRepository driverRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.seed-demo-users:true}") boolean enabled
    ) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        Long customerId = customerRepository.findAll().stream()
                .findFirst()
                .map(customer -> customer.getId())
                .orElse(null);
        Long driverId = driverRepository.findAll().stream()
                .findFirst()
                .map(driver -> driver.getId())
                .orElse(null);

        createIfMissing("admin@rotacerta.local", "Admin@123", "Administrador RotaCerta", UserRole.ADMIN, null, null);
        createIfMissing("driver@rotacerta.local", "Driver@123", "Motorista Demo", UserRole.DRIVER, null, driverId);
        createIfMissing("customer@rotacerta.local", "Customer@123", "Cliente Demo", UserRole.CUSTOMER, customerId, null);
    }

    private void createIfMissing(
            String email,
            String rawPassword,
            String displayName,
            UserRole role,
            Long customerId,
            Long driverId
    ) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        userRepository.save(new AppUser(
                email,
                passwordEncoder.encode(rawPassword),
                displayName,
                role,
                customerId,
                driverId
        ));
    }
}

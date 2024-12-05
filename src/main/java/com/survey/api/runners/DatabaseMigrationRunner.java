package com.survey.api.runners;

import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.IdentityUserRepository;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationRunner implements ApplicationRunner {
    @Autowired
    private Flyway flyway;

    @Autowired
    private IdentityUserRepository identityUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public DatabaseMigrationRunner(Flyway flyway, IdentityUserRepository identityUserRepository, PasswordEncoder passwordEncoder) {
        this.flyway = flyway;
        this.identityUserRepository = identityUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        flyway.migrate();
        addAdmin();
    }

    private void addAdmin() {
        String adminPassword = System.getenv("ADMIN_USER_PASSWORD");
        if (adminPassword == null) {
            throw new IllegalStateException("Admin password not set in environment variable.");
        }
        if (identityUserRepository.count() == 0) {
            IdentityUser identityUser = new IdentityUser();
            identityUser.setUsername("Admin");
            identityUser.setRole("Admin");
            identityUser.setPasswordHash(passwordEncoder.encode(adminPassword));
            identityUserRepository.save(identityUser);
        }
    }
}

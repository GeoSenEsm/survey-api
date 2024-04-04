package com.survey.api.runners;

import com.survey.application.dtos.GreeneryAreaCategoryDto;
import com.survey.domain.models.GreeneryAreaCategory;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.GreeneryAreaCategoryRepository;
import com.survey.domain.repository.IdentityUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseMigrationRunner implements ApplicationRunner {
    @Autowired
    private Flyway flyway;

    @Autowired
    private GreeneryAreaCategoryRepository repository;
    @Autowired
    private IdentityUserRepository identityUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private static final List<String> GREENERY_AREA_CATEGORY_NAMES = Arrays.asList("low-density", "medium-density", "high-density");


    @Override
    public void run(ApplicationArguments args) throws Exception {
        flyway.migrate();
        migrateGreeneryAreaCategories();
        addAdmin();

    }
    private void migrateGreeneryAreaCategories() {
        GREENERY_AREA_CATEGORY_NAMES.forEach(categoryName -> {
            if (!repository.existsByDisplay(categoryName)) {
                repository.save(new GreeneryAreaCategory(categoryName));
            }
        });
    }
    private void addAdmin() {
        if (identityUserRepository.count() == 0) {
            IdentityUser identityUser = new IdentityUser();
            identityUser.setUsername("Admin");
            identityUser.setRole("Admin");
            identityUser.setPasswordHash(passwordEncoder.encode("qwerty"));
            identityUserRepository.save(identityUser);
        }
    }
}

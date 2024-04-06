package com.survey.api.runners;

import com.survey.domain.models.AgeCategory;
import com.survey.domain.models.GreeneryAreaCategory;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.repository.AgeCategoryRepository;
import com.survey.domain.repository.GreeneryAreaCategoryRepository;
import com.survey.domain.repository.IdentityUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseMigrationRunner implements ApplicationRunner {
    @Autowired
    private Flyway flyway;

    @Autowired
    private GreeneryAreaCategoryRepository greeneryAreaRepository;
    @Autowired
    private AgeCategoryRepository ageCategoryRepository;
    @Autowired
    private IdentityUserRepository identityUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private static final List<String> GREENERY_AREA_CATEGORY_NAMES = Arrays.asList("low-density", "medium-density", "high-density");

    private static final List<String> AGE_CATEGORY_NAMES = Arrays.asList("50-59", "60-69", "70+");


    @Override
    public void run(ApplicationArguments args) throws Exception {
        flyway.migrate();
        migrateGreeneryAreaCategories();
        migrateAgeCategories();
        addAdmin();

    }
    private void migrateGreeneryAreaCategories() {
        GREENERY_AREA_CATEGORY_NAMES.forEach(categoryName -> {
            if (!greeneryAreaRepository.existsByDisplay(categoryName)) {
                greeneryAreaRepository.save(new GreeneryAreaCategory(categoryName));
            }
        });
    }

    private void migrateAgeCategories(){
        AGE_CATEGORY_NAMES.forEach(categoryName -> {
            if (!ageCategoryRepository.existsByDisplay(categoryName)){
                ageCategoryRepository.save(new AgeCategory(categoryName));
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

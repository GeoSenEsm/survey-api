package com.survey.api.runners;

import com.survey.domain.models.AgeCategory;
import com.survey.domain.models.GreeneryAreaCategory;
import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.OccupationCategory;
import com.survey.domain.repository.AgeCategoryRepository;
import com.survey.domain.repository.GreeneryAreaCategoryRepository;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.OccupationCategoryRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DatabaseMigrationRunner implements ApplicationRunner {
    @Autowired
    private Flyway flyway;

    @Autowired
    private GreeneryAreaCategoryRepository greeneryAreaCategoryRepository;
    @Autowired
    private IdentityUserRepository identityUserRepository;
    @Autowired
    private OccupationCategoryRepository occupationCategoryRepository;
    @Autowired
    private AgeCategoryRepository ageCategoryRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        flyway.migrate();
        addGreeneryAreaCategories();
        addOccupationCategories();
        addAgeCategories();
        addAdmin();
    }
    private void addGreeneryAreaCategories() {
        if (greeneryAreaCategoryRepository.count() == 0){
            List<GreeneryAreaCategory> occupationCategories = Stream.of("low-density", "medium-density", "high-density")
                    .map(GreeneryAreaCategory::new)
                    .collect(Collectors.toList());
            greeneryAreaCategoryRepository.saveAll(occupationCategories);
        }
    }

    private void addOccupationCategories(){
        if (occupationCategoryRepository.count() == 0){
            List<OccupationCategory> occupationCategories = Stream.of("employed", "unemployed")
                    .map(OccupationCategory::new)
                    .collect(Collectors.toList());
            occupationCategoryRepository.saveAll(occupationCategories);
        }
    }

    private void addAgeCategories(){
        if (ageCategoryRepository.count() == 0){
            List<AgeCategory> ageCategories = Stream.of("50-59", "60-69", "70+")
                    .map(AgeCategory::new)
                    .collect(Collectors.toList());
            ageCategoryRepository.saveAll(ageCategories);
        }
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

package com.survey.api.runners;

import com.survey.application.services.MedicationUseService;
import com.survey.domain.models.*;
import com.survey.domain.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
    private StressLevelRepository stressLevelRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private LifeSatisfactionRepository lifeSatisfactionRepository;
    @Autowired
    private HealthConditionRepository healthConditionRepository;
    @Autowired
    private EducationCategoryRepository educationCategoryRepository;
    @Autowired
    private QualityOfSleepRepository qualityOfSleepRepository;
    @Autowired
    private MedicationUseRepository medicationUseRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        flyway.migrate();
        addGreeneryAreaCategories();
        addOccupationCategories();
        addLifeSatisfaction();
        addAgeCategories();
        addStressLevels();
        addAdmin();
        addHealthConditions();
        addEducationCategories();
        addQualityOfSleep();
        addMedicationUse();
    }
    private void addGreeneryAreaCategories() {
        if (greeneryAreaCategoryRepository.count() == 0){
            List<GreeneryAreaCategory> greeneryAreaCategories =
                    Stream.of(
                            new GreeneryAreaCategory()
                                    .setPolishDisplay("małe zagęszczenie")
                                    .setEnglishDisplay("low density"),
                                    new GreeneryAreaCategory()
                                            .setPolishDisplay("średnie zagęszczenie")
                                            .setEnglishDisplay("medium density"),
                                    new GreeneryAreaCategory()
                                            .setPolishDisplay("wysokie zagęszczenie")
                                            .setEnglishDisplay("high density"))
                    .collect(Collectors.toList());
            greeneryAreaCategoryRepository.saveAll(greeneryAreaCategories);
        }
    }

    private void addOccupationCategories(){
        if (occupationCategoryRepository.count() == 0){
            List<OccupationCategory> occupationCategories = Stream.of(
                            new OccupationCategory().setPolishDisplay("zatrudniony").setEnglishDisplay("employed"),
                            new OccupationCategory().setPolishDisplay("niezatrudniony").setEnglishDisplay("unemployed"))
                    .collect(Collectors.toList());
            occupationCategoryRepository.saveAll(occupationCategories);
        }
    }

    private void addLifeSatisfaction(){
        if (lifeSatisfactionRepository.count() == 0){
            List<LifeSatisfaction> lifeSatisfactions = Stream.of(
                            new LifeSatisfaction().setPolishDisplay("niskie").setEnglishDisplay("low"),
                            new LifeSatisfaction().setPolishDisplay("średnie").setEnglishDisplay("medium"),
                            new LifeSatisfaction().setPolishDisplay("wysokie").setEnglishDisplay("high"))
                    .collect(Collectors.toList());
            lifeSatisfactionRepository.saveAll(lifeSatisfactions);
        }
    }

    private void addAgeCategories(){
        if (ageCategoryRepository.count() == 0){
            List<AgeCategory> ageCategories = Stream.of(
                            new AgeCategory().setPolishDisplay("50-59").setEnglishDisplay("50-59"),
                            new AgeCategory().setPolishDisplay("60-69").setEnglishDisplay("60-69"),
                            new AgeCategory().setPolishDisplay("70+").setEnglishDisplay("70+"))
                    .collect(Collectors.toList());
            ageCategoryRepository.saveAll(ageCategories);
        }
    }

    private void addStressLevels(){
        if (stressLevelRepository.count() == 0){
            List<StressLevel> stressLevels = Stream.of(
                            new StressLevel().setPolishDisplay("niski").setEnglishDisplay("low"),
                            new StressLevel().setPolishDisplay("średni").setEnglishDisplay("medium"),
                            new StressLevel().setPolishDisplay("wysoki").setEnglishDisplay("high"))
                    .collect(Collectors.toList());
            stressLevelRepository.saveAll(stressLevels);
        }
    }

    private void addHealthConditions() {
        if (healthConditionRepository.count() == 0) {
            List<HealthCondition> healthConditions = Stream.of(
                            new HealthCondition().setPolishDisplay("niska").setEnglishDisplay("low"),
                            new HealthCondition().setPolishDisplay("średnia").setEnglishDisplay("medium"),
                            new HealthCondition().setPolishDisplay("wysoka").setEnglishDisplay("high"))
                    .collect(Collectors.toList());
            healthConditionRepository.saveAll(healthConditions);
        }
    }

    private void addEducationCategories() {
        if (educationCategoryRepository.count() == 0) {
            List<EducationCategory> educationCategories = Stream.of(
                            new EducationCategory().setPolishDisplay("podstawowe").setEnglishDisplay("primary"),
                            new EducationCategory().setPolishDisplay("średnie").setEnglishDisplay("secondary"),
                            new EducationCategory().setPolishDisplay("wyższe").setEnglishDisplay("higher"),
                            new EducationCategory().setPolishDisplay("zawodowe").setEnglishDisplay("vocational"))
                    .collect(Collectors.toList());
            educationCategoryRepository.saveAll(educationCategories);
        }
    }

    private void addQualityOfSleep() {
        if (qualityOfSleepRepository.count() == 0) {
            List<QualityOfSleep> qualityOfSleep = Stream.of(
                            new QualityOfSleep().setPolishDisplay("niska").setEnglishDisplay("low"),
                            new QualityOfSleep().setPolishDisplay("średnia").setEnglishDisplay("medium"),
                            new QualityOfSleep().setPolishDisplay("wysoka").setEnglishDisplay("high"))
                    .collect(Collectors.toList());
            qualityOfSleepRepository.saveAll(qualityOfSleep);
        }
    }

    private void addMedicationUse() {
        if (medicationUseRepository.count() == 0) {
            List<MedicationUse> medicationUses = Stream.of(
                            new MedicationUse().setPolishDisplay("tak").setEnglishDisplay("yes"),
                            new MedicationUse().setPolishDisplay("nie").setEnglishDisplay("no"))
                    .collect(Collectors.toList());
            medicationUseRepository.saveAll(medicationUses);
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

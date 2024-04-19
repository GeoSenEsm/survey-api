CREATE TABLE respondent_data (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    gender_id INT,
    age_category_id INT,
    occupation_category_id INT,
    education_category_id INT,
    health_condition_id INT,
    medication_use_id INT,
    life_satisfaction_id INT,
    stress_level_id INT,
    quality_of_sleep_id INT,
);

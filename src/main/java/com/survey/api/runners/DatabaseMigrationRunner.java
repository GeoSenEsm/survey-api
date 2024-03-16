package com.survey.api.runners;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationRunner implements ApplicationRunner {
    @Autowired
    private Flyway flyway;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        flyway.migrate();
    }
}

package com.campus.platform.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ErrandSchemaInitializer implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(false, false, "UTF-8",
            new ClassPathResource("db/errand-schema.sql"));
        populator.execute(dataSource);
    }
}

package com.survey.api.configuration;

import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.Flyway;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.security.Timestamp;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
@Setter
@Getter
public class AppConfig {

    private String url;
    private String user;
    private String password;
    private String driverClassName;

    @Bean
    public Flyway flyway(){
        DataSource ds = DataSourceBuilder.create()
                .url(url)
                .username(user)
                .password(password)
                .driverClassName(driverClassName)
                .build();

        return Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .load();
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addConverter(new AbstractConverter<byte[], Long>() {
            @Override
            protected Long convert(byte[] timestampBytes) {
                if (timestampBytes == null || timestampBytes.length != 8) {
                    throw new IllegalArgumentException("Invalid timestamp byte array");
                }

                ByteBuffer buffer = ByteBuffer.wrap(timestampBytes);
                return buffer.getLong();
            }
        });

        return modelMapper;
    }
}

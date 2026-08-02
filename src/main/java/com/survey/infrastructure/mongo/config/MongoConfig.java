package com.survey.infrastructure.mongo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Persists {@link OffsetDateTime} as ISO-8601 strings so the UTC offset is
 * preserved. Spring Data Mongo's built-in JSR-310 converter maps to
 * {@code java.util.Date}, which silently drops the offset. LocalDate /
 * LocalTime are stored as ISO strings for study wall-clock filters.
 */
@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new OffsetDateTimeToStringConverter(),
                new StringToOffsetDateTimeConverter(),
                new LocalDateToStringConverter(),
                new StringToLocalDateConverter(),
                new LocalTimeToStringConverter(),
                new StringToLocalTimeConverter()
        ));
    }

    @WritingConverter
    static class OffsetDateTimeToStringConverter implements Converter<OffsetDateTime, String> {
        @Override
        public String convert(OffsetDateTime source) {
            return source.withOffsetSameInstant(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

    @ReadingConverter
    static class StringToOffsetDateTimeConverter implements Converter<String, OffsetDateTime> {
        @Override
        public OffsetDateTime convert(String source) {
            return OffsetDateTime.parse(source, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

    @WritingConverter
    static class LocalDateToStringConverter implements Converter<LocalDate, String> {
        @Override
        public String convert(LocalDate source) {
            return source.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }

    @ReadingConverter
    static class StringToLocalDateConverter implements Converter<String, LocalDate> {
        @Override
        public LocalDate convert(String source) {
            return LocalDate.parse(source, DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }

    @WritingConverter
    static class LocalTimeToStringConverter implements Converter<LocalTime, String> {
        @Override
        public String convert(LocalTime source) {
            return source.format(DateTimeFormatter.ISO_LOCAL_TIME);
        }
    }

    @ReadingConverter
    static class StringToLocalTimeConverter implements Converter<String, LocalTime> {
        @Override
        public LocalTime convert(String source) {
            return LocalTime.parse(source, DateTimeFormatter.ISO_LOCAL_TIME);
        }
    }
}

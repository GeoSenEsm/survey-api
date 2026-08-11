package com.survey.infrastructure.mongo.documents;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Denormalized snapshot of a single survey response. Written after the SQL
 * transaction commits (see {@code SurveyResponseSubmittedEvent}) and read back
 * by the admin "Response Documents" view. The {@code _id} is the SQL
 * participation UUID, which keeps SQL and Mongo aligned without an extra key.
 *
 * Null / empty properties are omitted from both the Mongo document
 * ({@code @Field(write = NON_NULL)}) and the JSON output
 * ({@code @JsonInclude}). This keeps each answer to only the fields that
 * actually carry a value for its question type.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "surveyResponseDocuments")
public class SurveyResponseDocument {

    @Id
    private UUID participationId;

    @Indexed
    private UUID surveyId;
    private String surveyName;

    @Indexed
    private UUID respondentId;
    private String respondentUsername;

    @Indexed
    private OffsetDateTime participationDate;
    /** Wall-clock calendar day in the respondent's timezone. */
    @Indexed
    private LocalDate localDate;
    /** Wall-clock time of day in the respondent's timezone. */
    private LocalTime localTime;
    private OffsetDateTime surveyStartDate;
    private OffsetDateTime surveyFinishDate;

    private List<Answer> answers;

    @Field(write = Field.Write.NON_NULL)
    private List<SensorReading> sensorData;

    private OffsetDateTime persistedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Answer {
        private UUID questionId;
        private String questionContent;
        private String questionType;

        @Field(write = Field.Write.NON_NULL)
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<SelectedOption> selectedOptions;

        @Field(write = Field.Write.NON_NULL)
        private Integer numericAnswer;

        @Field(write = Field.Write.NON_NULL)
        private Boolean yesNoAnswer;

        @Field(write = Field.Write.NON_NULL)
        private String textAnswer;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SelectedOption {
        private UUID optionId;
        private String label;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SensorReading {
        private OffsetDateTime dateTime;
        private String source;
        private List<SensorValue> values;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SensorValue {
        private String parameterCode;
        private String value;
    }
}

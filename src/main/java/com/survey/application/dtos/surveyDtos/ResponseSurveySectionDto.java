package com.survey.application.dtos.surveyDtos;

import com.survey.domain.models.enums.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseSurveySectionDto {
    @Schema(description = "UUID of the survey section.")
    private UUID id;

    @Schema(description = "Order in which sections will be displayed in the survey. Unique within the scope of the survey.",
            example = "1",
        minimum = "1",
        maximum = "9999")
    private Integer order;

    @Schema(description = "The name of the section. Can be null.",
            example = "My first survey section",
            maximum = "100")
    private String name;

    @Schema(description = "The visibility of the section.",
            enumAsRef = true,
            allowableValues = {"`always`", "`group_specific`", "`answer_triggered`"} )
    private Visibility visibility;

    @Schema(description = "(optional) When section visibility is set to `group_specific, this parameter will be present and will contain UUID of the user group that this section is visible to.`")
    private UUID groupId;

    @Schema(description = "Indicates whether the section should be displayed on a single screen or each question should be visible on new screen.",
            example = "true")
    private Boolean displayOnOneScreen;

    @Schema(description = "A list of questions within the section. Must contain at least one valid question.")
    private List<ResponseQuestionDto> questions;

    private Long rowVersion;
}

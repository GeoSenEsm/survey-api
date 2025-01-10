package com.survey.application.dtos.surveyDtos;

import com.survey.api.validation.ValidVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateSurveySectionDto {

    @NotNull
    @Min(1)
    @Max(9999)
    @Schema(description = "Order in which sections will be displayed in the survey. Unique within the scope of the survey.",
            example = "1")
    private Integer order;

    @Size(max = 100)
    @Schema(description = "The name of the section. Can be null.",
            example = "My first survey section")
    private String name;

    @NotNull
    @ValidVisibility
    @Schema(description = "The visibility of the section.",
            enumAsRef = true,
            allowableValues = {"`always`", "`group_specific`", "`answer_triggered`"} )
    private String visibility;

    @Schema(description = "(optional) Must be set when section visibility is set to `group_specific`. Specifies UUID of group that this section will be visible to.")
    private String groupId;

    @NotNull
    @Schema(description = "Indicates whether the section should be displayed on a single screen or each question should be visible on new screen.",
            example = "true")
    private boolean displayOnOneScreen;

    @NotEmpty
    private List<@Valid CreateQuestionDto> questions;

}

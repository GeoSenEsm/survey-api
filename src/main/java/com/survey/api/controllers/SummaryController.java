package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.HistogramDataDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/summaries")
@Tag(name = "Histogram", description = "Fetch data to display histogram.")
public class SummaryController {
    private final SummaryService summaryService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public SummaryController(SummaryService summaryService, ClaimsPrincipalService claimsPrincipalService) {
        this.summaryService = summaryService;
        this.claimsPrincipalService = claimsPrincipalService;
    }


    @GetMapping("/histogram")
    @Operation(
            summary = "Fetch data to display histogram",
            description = """
                    - Endpoint deprecated.
                    - Admin panel does not display charts.
                    - Must be updated to handle new question types.
                    - **Access:**
                        - RESPONDENT
                    """,
            deprecated = true)
    @CommonApiResponse403
    public ResponseEntity<List<HistogramDataDto>> getHistogramData(
            @RequestParam("surveyId") UUID surveyId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date date){

        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());

        List<HistogramDataDto> histogramDataDtoList = summaryService.getHistogramData(surveyId, date);
        return ResponseEntity.ok(histogramDataDtoList);
    }
}

package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.statistics.DailyCompletionOverviewDto;
import com.survey.application.dtos.statistics.GlobalStatsDetailDto;
import com.survey.application.dtos.statistics.ParticipantStatsDetailDto;
import com.survey.application.dtos.statistics.ParticipantStatsDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Study-wide and per-participant aggregates for the admin
 * "Statistics" tab. All endpoints are ADMIN-only (enforced
 * programmatically — see {@link ClaimsPrincipalService}).
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Study aggregates for the admin dashboard.")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @GetMapping("/participants")
    @Operation(
            summary = "List participant statistics.",
            description = "One summary row per respondent with at least one submitted response, sorted by surveys filled desc. **Access:** ADMIN")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Participant list.")})
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<ParticipantStatsDto>> listParticipants() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.ok(statisticsService.listParticipantStats());
    }

    @GetMapping("/participants/{respondentId}")
    @Operation(
            summary = "Fetch stats + daily time series for one respondent.",
            description = "**Access:** ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Participant detail."),
            @ApiResponse(responseCode = "404", description = "Respondent has never submitted a response.")
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<ParticipantStatsDetailDto> getParticipantDetail(
            @PathVariable("respondentId") UUID respondentId) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.ok(statisticsService.getParticipantDetail(respondentId));
    }

    @GetMapping("/global")
    @Operation(
            summary = "Fetch overall study aggregates + daily time series.",
            description = "**Access:** ADMIN")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Global stats.")})
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<GlobalStatsDetailDto> getGlobal() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.ok(statisticsService.getGlobalDetail());
    }

    @GetMapping("/daily-completion")
    @Operation(
            summary = "Full-day survey completion overview.",
            description = """
                - Lists every time slot whose window overlaps the given UTC day.
                - For every respondent account, reports which of those slots
                  they have already filled (by ID) together with the count.
                - The client uses `finish` and the current time to distinguish
                  "missed" (finish in the past, not in completed set) from
                  "pending" (finish in the future, not in completed set).
                - **Access:** ADMIN
                """)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Daily completion overview.")})
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<DailyCompletionOverviewDto> getDailyCompletion(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        return ResponseEntity.ok(statisticsService.getDailyCompletion(date));
    }
}

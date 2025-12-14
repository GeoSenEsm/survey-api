package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.ResearchAreaDto;
import com.survey.application.dtos.ResponseResearchAreaDto;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.ResearchAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/researcharea")
@Tag(name = "Research Area", description = "Endpoints for managing research area.")
public class ResearchAreaController {
    private final ResearchAreaService researchAreaService;
    private final ClaimsPrincipalService claimsPrincipalService;

    public ResearchAreaController(ResearchAreaService researchAreaService, ClaimsPrincipalService claimsPrincipalService) {
        this.researchAreaService = researchAreaService;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @PostMapping
    @Operation(
            summary = "Create research area polygon.",
            description = """
                    - **IMPORTANT**
                        - First and last localization point must be the same (so the polygon is "closed").
                        - Points must be send in counter clockwise order.
                    - Endpoint takes a list of localization data points (latitude, longitude) in order to define research area.
                    - Research area is just a polygon that defines the area in which respondents should be.
                    - Research area polygon can be created with min. 3, max. 250 geolocation points.
                    - It is not mandatory to set research area. If it it set, it will be possible to filter survey responses that have been sent from outside this area.
                    - What if respondents already sent their localization data but I want to change research area?
                        - When new research area polygon is uploaded, all currently existing localization data points have their `outsideResearchArea` parameter recalculated.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Research area polygon created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ResponseResearchAreaDto.class))
                    ))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<ResponseResearchAreaDto>> saveResearchAreaData(@Valid @RequestBody List<ResearchAreaDto> researchAreaDtoList) throws BadRequestException {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<ResponseResearchAreaDto> responseResearchAreaDto = researchAreaService.saveResearchArea(researchAreaDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseResearchAreaDto);
    }


    @GetMapping
    @Operation(
            summary = "Fetch research area polygon.",
            description = """
                    - Returns a list of localization points that create the research area polygon.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Research area polygon fetched successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ResponseResearchAreaDto.class))
                    )),
            @ApiResponse(responseCode = "404",
                    description = "Research area polygon has not been defined yet.",
                    content = @Content(mediaType = "null"))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<ResponseResearchAreaDto>> getResearchArea() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        List<ResponseResearchAreaDto> responseResearchAreaDtoList = researchAreaService.getResearchArea();
        if (responseResearchAreaDtoList != null) {
            return ResponseEntity.ok(responseResearchAreaDtoList);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }


    @DeleteMapping
    @Operation(
            summary = "Delete research area polygon.",
            description = """
                    - Deletes research area polygon if it exists.
                    - Parameter `outsideResearchArea` of respondents localization points that are already in the database will be set to NULL.
                    - **Access:**
                        - ADMIN
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Research area polygon deleted successfully."),
            @ApiResponse(responseCode = "404",
                    description = "Research area polygon has not been defined yet.")
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<Void> deleteResearchArea() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        boolean isDeleted = researchAreaService.deleteResearchArea();
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}

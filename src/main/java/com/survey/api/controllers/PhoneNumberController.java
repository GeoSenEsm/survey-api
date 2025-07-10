package com.survey.api.controllers;

import com.survey.api.configuration.CommonApiResponse400;
import com.survey.api.configuration.CommonApiResponse401;
import com.survey.api.configuration.CommonApiResponse403;
import com.survey.api.security.Role;
import com.survey.application.dtos.PhoneNumberDtoIn;
import com.survey.application.dtos.PhoneNumberDtoOut;
import com.survey.application.services.ClaimsPrincipalService;
import com.survey.application.services.PhoneNumberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/phonenumber")
@Tag(name = "Phone Number", description = "Endpoints for managing phone numbers.")
public class PhoneNumberController {
    private final PhoneNumberService phoneNumberService;
    private final ClaimsPrincipalService claimsPrincipalService;

    @Autowired
    public PhoneNumberController(PhoneNumberService phoneNumberService, ClaimsPrincipalService claimsPrincipalService) {
        this.phoneNumberService = phoneNumberService;
        this.claimsPrincipalService = claimsPrincipalService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new phone number record.",
            description = """
                    - Allows creation of a new phone number record in the system.
                    - Requires a name and a phone number in the request body.
                    - The phone number may start with an optional plus sign (`+`) and digits may be separated by optional separators such as a dash (`-`), dot (`.`), or whitespace.
                    - The generated unique ID (UUID) for the new record will be returned in the response.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Phone number created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PhoneNumberDtoOut.class)))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<PhoneNumberDtoOut> createPhoneNumber(@Valid @RequestBody PhoneNumberDtoIn phoneNumberDtoIn) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        PhoneNumberDtoOut createdPhoneNumber = phoneNumberService.createPhoneNumber(phoneNumberDtoIn);
        return new ResponseEntity<>(createdPhoneNumber, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Retrieve all phone number records.",
            description = """
                    - Fetches a list of all phone numbers currently stored in the system.
                    - The list can be empty if no phone numbers exist.
                    - **Access:**
                        - ADMIN
                        - RESPONDENT
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of phone numbers.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PhoneNumberDtoOut.class))))
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<List<PhoneNumberDtoOut>> getAllPhoneNumbers() {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        List<PhoneNumberDtoOut> phoneNumbers = phoneNumberService.getAllPhoneNumbers();
        return new ResponseEntity<>(phoneNumbers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Retrieve a phone number by its unique ID.",
            description = """
                    - Fetches details of a specific phone number using its unique identifier (UUID).
                    - Returns 404 Not Found if the phone number does not exist.
                    - **Access:**
                        - ADMIN
                        - RESPONDENT
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved phone number.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PhoneNumberDtoOut.class))),
            @ApiResponse(responseCode = "404", description = "Phone number not found with the given ID.",
                    content = @Content(mediaType = "application/json"))
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<PhoneNumberDtoOut> getPhoneNumberById(@PathVariable UUID id) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName(), Role.RESPONDENT.getRoleName());
        return phoneNumberService.getPhoneNumberById(id)
                .map(phoneNumberDtoOut -> new ResponseEntity<>(phoneNumberDtoOut, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing phone number record.",
            description = """
                    - Updates the details of an existing phone number identified by its unique ID.
                    - The request body should contain the updated name and number.
                    - Returns 404 Not Found if the phone number does not exist.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Phone number updated successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PhoneNumberDtoOut.class))),
            @ApiResponse(responseCode = "404", description = "Phone number not found with the given ID.",
                    content = @Content(mediaType = "application/json"))
    })
    @CommonApiResponse400
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<PhoneNumberDtoOut> updatePhoneNumber(@PathVariable UUID id, @Valid @RequestBody PhoneNumberDtoIn phoneNumberDtoIn) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        try {
            PhoneNumberDtoOut updatedPhoneNumber = phoneNumberService.updatePhoneNumber(id, phoneNumberDtoIn);
            return new ResponseEntity<>(updatedPhoneNumber, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a phone number record.",
            description = """
                    - Deletes a phone number record from the system using its unique ID.
                    - Returns 204 No Content upon successful deletion.
                    - Returns 404 Not Found if the phone number does not exist.
                    - **Access:**
                        - ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Phone number deleted successfully (No Content)."),
            @ApiResponse(responseCode = "404", description = "Phone number not found with the given ID.",
                    content = @Content(mediaType = "application/json"))
    })
    @CommonApiResponse401
    @CommonApiResponse403
    public ResponseEntity<Void> deletePhoneNumber(@PathVariable UUID id) {
        claimsPrincipalService.ensureRole(Role.ADMIN.getRoleName());
        try {
            phoneNumberService.deletePhoneNumber(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

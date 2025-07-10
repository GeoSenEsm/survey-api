package com.survey.application.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhoneNumberDtoIn {
    @NotNull
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @NotNull
    @Pattern(regexp = "^[+]?[0-9]{1,3}[-.\\s]?[0-9]{3,}[-.\\s]?[0-9]{3,}[-.\\s]?[0-9]{2,}[-.\\s]?[0-9]*$",
            message = "Invalid phone number format")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String number;
}

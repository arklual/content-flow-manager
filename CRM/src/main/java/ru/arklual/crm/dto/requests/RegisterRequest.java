package ru.arklual.crm.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size
    private String name;

    @NotBlank
    private String password;
}

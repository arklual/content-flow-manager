package ru.arklual.crm.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

}
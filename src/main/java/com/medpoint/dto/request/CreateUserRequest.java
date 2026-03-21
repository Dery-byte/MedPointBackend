package com.medpoint.dto.request;
import com.medpoint.enums.AccessModule;
import com.medpoint.enums.ManageModule;
import com.medpoint.enums.StaffRole;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Set;

@Data
public class CreateUserRequest {
    @NotBlank private String name;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    @NotNull private StaffRole role;
    @NotEmpty(message = "At least one access module must be assigned") private Set<AccessModule> accessModules;
    private Set<ManageModule> manageModules;
}

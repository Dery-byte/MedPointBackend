package com.medpoint.dto.request;
import com.medpoint.enums.AccessModule;
import com.medpoint.enums.ManageModule;
import com.medpoint.enums.StaffRole;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Set;

@Data
public class UpdateUserRequest {
    @NotBlank private String name;
    @NotNull private StaffRole role;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    /** Null or blank = keep existing password. */
    private String password;
    @NotEmpty(message = "At least one access module must be assigned") private Set<AccessModule> accessModules;
    private Set<ManageModule> manageModules;
}

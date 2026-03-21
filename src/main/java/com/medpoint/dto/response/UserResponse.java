package com.medpoint.dto.response;
import com.medpoint.enums.AccessModule;
import com.medpoint.enums.ManageModule;
import com.medpoint.enums.StaffRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.Set;

@Data @Builder
public class UserResponse {
    private Long id;
    private String name;

    private String firstName;
    private String lastName;
    private String email;
    private StaffRole role;
    private Set<AccessModule> accessModules;
    private Set<ManageModule> manageModules;
    private boolean active;
    private Instant createdAt;
}

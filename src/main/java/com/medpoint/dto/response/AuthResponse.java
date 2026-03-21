package com.medpoint.dto.response;
import com.medpoint.enums.AccessModule;
import com.medpoint.enums.ManageModule;
import com.medpoint.enums.StaffRole;
import lombok.Builder;
import lombok.Data;
import java.util.Set;

/**
 * Flat auth response — matches the frontend types.ts AuthResponse exactly.
 * Frontend expects: { token, tokenType, id, name, email, role, accessModules, manageModules, active }
 */
@Data @Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    // Flat user fields (no nested object)
    private Long id;
    private String name;
    private String email;
    private StaffRole role;
    private Set<AccessModule> accessModules;
    private Set<ManageModule> manageModules;
    private boolean active;
}

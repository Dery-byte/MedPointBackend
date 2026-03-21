package com.medpoint.entity;

import com.medpoint.enums.AccessModule;
import com.medpoint.enums.ManageModule;
import com.medpoint.enums.StaffRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;

/**
 * Represents a MedPoint staff member.
 *
 * accessModules – modules this user can operate (use the POS / check-in desk).
 * manageModules – modules this user can administer (add/edit/price catalogue items).
 *                 SUPERADMIN implicitly holds both for every module.
 */
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;


    @Column(nullable = true)
    private String lastName;

    @Column(nullable = true)
    private String firstName;



    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffRole role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_access_modules", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "module")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<AccessModule> accessModules = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_manage_modules", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "module")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<ManageModule> manageModules = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    // ── UserDetails impl ─────────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getUsername()              { return email; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return active; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return active; }

    /** Convenience guard used in service layer. */
    public boolean canAccess(AccessModule m) {
        return role == StaffRole.SUPERADMIN || accessModules.contains(m);
    }

    public boolean canManage(ManageModule m) {
        return role == StaffRole.SUPERADMIN || manageModules.contains(m);
    }
}

package com.medpoint.security;

import com.medpoint.entity.Developer;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class DevDetails implements UserDetails {

    @Getter
    private final Long id;
    private final String email;
    private final String name;

    public DevDetails(Developer developer) {
        this.id    = developer.getId();
        this.email = developer.getEmail();
        this.name  = developer.getName();
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_DEV"));
    }

    @Override public String getPassword()  { return ""; }
    @Override public String getUsername()  { return email; }

    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return true; }

    public String getName() { return name; }
}

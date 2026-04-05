package com.medpoint.security;

import com.medpoint.repository.DeveloperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("devDetailsService")
@RequiredArgsConstructor
public class DevDetailsService implements UserDetailsService {

    private final DeveloperRepository developerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return developerRepository.findByEmail(email)
                .map(DevDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Developer not found: " + email));
    }
}

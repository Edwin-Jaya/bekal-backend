package org.edwin.bekal.config.security;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.edwin.bekal.domain.master.repository.InternalUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InternalUserDetailsService implements UserDetailsService {

    private final InternalUserRepository internalUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String internalUserEmail) throws UsernameNotFoundException {
        InternalUser internalUser = internalUserRepository.findByInternalUserEmailAndInternalUserIsActiveTrue(internalUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + internalUserEmail));

        return new User(
                internalUser.getInternalUserEmail(),
                internalUser.getInternalUserPasswordHash(),
                internalUser.getInternalUserIsActive(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + internalUser.getRole().getRoleName().toUpperCase()))
        );
    }
}
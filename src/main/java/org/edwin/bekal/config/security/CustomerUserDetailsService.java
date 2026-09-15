package org.edwin.bekal.config.security;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
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
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String customerEmail) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByCustomerEmail(customerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with email: " + customerEmail));

        return new User(
                customer.getCustomerEmail(),
                customer.getCustomerPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }
}
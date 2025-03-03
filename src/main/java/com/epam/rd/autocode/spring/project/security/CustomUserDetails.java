package com.epam.rd.autocode.spring.project.security;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.Collections;


public class CustomUserDetails implements UserDetails {
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Client client) {
        this.email = client.getEmail();
        this.password = client.getPassword();
        this.authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_CLIENT"));
    }

    public CustomUserDetails(Employee employee) {
        this.email = employee.getEmail();
        this.password = employee.getPassword();
        this.authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

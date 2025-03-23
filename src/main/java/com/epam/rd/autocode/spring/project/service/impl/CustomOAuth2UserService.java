package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final ClientRepository clientRepository;

    public CustomOAuth2UserService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Check if the user already exists in your database
        Optional<Client> existingClient = clientRepository.findByEmail(email);
        if (existingClient.isEmpty()) {
            // Create a new client in your database
            Client newClient = new Client();
            newClient.setEmail(email);
            newClient.setName(name);
            newClient.setPassword("$2y$10$Vkz6Y9Bz4gp/yeNCgVf4ZuWEkL8yArVro.s9Qh7cnUpTu/67aElQa"); // OAuth2 users don't need a password
            newClient.setBalance(BigDecimal.valueOf(0.0)); // Default balance

            // Save the new client
            clientRepository.save(newClient);
        }

        return new CustomOAuth2User(oAuth2User,"ROLE_CLIENT");
    }

    private static class CustomOAuth2User implements OAuth2User{
        private final OAuth2User oAuth2User;
        private final String role;

        public CustomOAuth2User(OAuth2User oAuth2User, String role) {
            this.oAuth2User = oAuth2User;
            this.role = role;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return oAuth2User.getAttributes();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singleton(new SimpleGrantedAuthority(role));
        }

        @Override
        public String getName() {
            return oAuth2User.getAttribute("email");
        }
    }
}
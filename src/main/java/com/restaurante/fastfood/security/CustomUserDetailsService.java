// src/main/java/com/restaurante/fastfood/security/CustomUserDetailsService.java
package com.restaurante.fastfood.security;

import com.restaurante.fastfood.model.User;
import com.restaurante.fastfood.model.enums.Role;
import com.restaurante.fastfood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Permitir login por username o email
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o email no encontrado: " + usernameOrEmail));

        if (!user.isEnabled() && user.getRole() == Role.ROLE_CLIENT) {
            throw new UsernameNotFoundException("Tu cuenta ha sido desactivada. Contacta con soporte.");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true,
                Collections.singleton(
                        new SimpleGrantedAuthority(user.getRole().name())
                )
        );
    }
}

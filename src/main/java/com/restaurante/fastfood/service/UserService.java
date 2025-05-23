// src/main/java/com/restaurante/fastfood/service/UserService.java
package com.restaurante.fastfood.service;

import com.restaurante.fastfood.model.User;
import com.restaurante.fastfood.model.enums.Role;
import com.restaurante.fastfood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerNewUser(User user) {
        // Verificar si el usuario ya existe
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }

        // Codificar la contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Por defecto, asignar rol de cliente
        if (user.getRole() == null) {
            user.setRole(Role.ROLE_CLIENT);
        }

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<User> searchUsers(String query, String role) {
        boolean hasQuery = query != null && !query.isEmpty();
        boolean hasRole = role != null && !role.isEmpty();
        String safeQuery = (query == null) ? "" : query;

        if (!hasQuery && !hasRole) {
            return userRepository.findAll();
        }
        if (hasQuery && !hasRole) {
            return userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                safeQuery, safeQuery, safeQuery
            );
        }
        if (!hasQuery && hasRole) {
            com.restaurante.fastfood.model.enums.Role roleEnum;
            try {
                roleEnum = com.restaurante.fastfood.model.enums.Role.valueOf(role);
            } catch (Exception e) {
                return userRepository.findAll();
            }
            return userRepository.findByRole(roleEnum);
        }
        // Ambos filtros: primero filtra por rol, luego por texto en memoria
        com.restaurante.fastfood.model.enums.Role roleEnum;
        try {
            roleEnum = com.restaurante.fastfood.model.enums.Role.valueOf(role);
        } catch (Exception e) {
            return userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                safeQuery, safeQuery, safeQuery
            );
        }
        return userRepository.findByRole(roleEnum).stream()
            .filter(user ->
                (user.getUsername() != null && user.getUsername().toLowerCase().contains(safeQuery.toLowerCase())) ||
                (user.getFullName() != null && user.getFullName().toLowerCase().contains(safeQuery.toLowerCase())) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(safeQuery.toLowerCase()))
            )
            .collect(java.util.stream.Collectors.toList());
    }
}

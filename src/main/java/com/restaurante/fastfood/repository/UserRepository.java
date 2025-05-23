// src/main/java/com/restaurante/fastfood/repository/UserRepository.java
package com.restaurante.fastfood.repository;

import com.restaurante.fastfood.model.User;
import com.restaurante.fastfood.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Buscar por username, nombre o email (cualquiera contiene el query)
    List<User> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String fullName, String email);
    // Buscar por rol exacto (quita @Nullable para evitar ambigüedad y errores de JPA con enums)
    List<User> findByRole(com.restaurante.fastfood.model.enums.Role role);
    // Buscar por rol y query (quita @Nullable para evitar ambigüedad)
    List<User> findByRoleAndUsernameContainingIgnoreCaseOrRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(com.restaurante.fastfood.model.enums.Role role1, String username, com.restaurante.fastfood.model.enums.Role role2, String fullName, com.restaurante.fastfood.model.enums.Role role3, String email);
}

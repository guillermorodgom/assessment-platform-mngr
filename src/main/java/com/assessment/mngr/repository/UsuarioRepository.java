package com.assessment.mngr.repository;

import com.assessment.mngr.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    @Query(value = "SELECT u.* FROM usuarios u INNER JOIN user_roles r ON u.id = r.usuario_id WHERE r.rol = 'CANDIDATO'", nativeQuery = true)
    List<Usuario> findCandidatos();
}

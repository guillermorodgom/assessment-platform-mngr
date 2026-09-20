package com.assessment.mngr.repository;

import com.assessment.mngr.model.Cuestionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CuestionarioRepository extends JpaRepository<Cuestionario, Long> {
    List<Cuestionario> findByActivoTrue();
    List<Cuestionario> findByCreadoPorId(Long usuarioId);
}

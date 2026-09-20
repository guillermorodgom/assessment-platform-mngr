package com.assessment.mngr.repository;

import com.assessment.mngr.model.AsignacionCuestionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsignacionCuestionarioRepository extends JpaRepository<AsignacionCuestionario, Long> {

    List<AsignacionCuestionario> findByCandidatoId(Long candidatoId);

    List<AsignacionCuestionario> findByCuestionarioId(Long cuestionarioId);

    Optional<AsignacionCuestionario> findByCuestionarioIdAndCandidatoId(Long cuestionarioId, Long candidatoId);

    boolean existsByCuestionarioIdAndCandidatoId(Long cuestionarioId, Long candidatoId);
}

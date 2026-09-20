package com.assessment.mngr.repository;

import com.assessment.mngr.model.IntentoExamen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntentoExamenRepository extends JpaRepository<IntentoExamen, Long> {
    List<IntentoExamen> findByCandidatoId(Long candidatoId);
    List<IntentoExamen> findByCuestionarioId(Long cuestionarioId);
    List<IntentoExamen> findByCandidatoIdAndCuestionarioId(Long candidatoId, Long cuestionarioId);
}

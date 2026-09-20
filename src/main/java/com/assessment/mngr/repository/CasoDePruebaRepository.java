package com.assessment.mngr.repository;

import com.assessment.mngr.model.CasoDePrueba;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CasoDePruebaRepository extends JpaRepository<CasoDePrueba, Long> {
    List<CasoDePrueba> findByPreguntaId(Long preguntaId);
}

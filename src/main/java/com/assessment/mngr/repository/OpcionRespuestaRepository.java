package com.assessment.mngr.repository;

import com.assessment.mngr.model.OpcionRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpcionRespuestaRepository extends JpaRepository<OpcionRespuesta, Long> {
    List<OpcionRespuesta> findByPreguntaId(Long preguntaId);
}

package com.assessment.mngr.repository;

import com.assessment.mngr.model.RespuestaCandidato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RespuestaCandidatoRepository extends JpaRepository<RespuestaCandidato, Long> {
    List<RespuestaCandidato> findByIntentoExamenId(Long intentoExamenId);
    Optional<RespuestaCandidato> findByIntentoExamenIdAndPreguntaId(Long intentoExamenId, Long preguntaId);
}

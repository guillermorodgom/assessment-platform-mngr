package com.assessment.mngr.repository;

import com.assessment.mngr.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    @Query("SELECT p FROM Pregunta p JOIN p.cuestionarios c WHERE c.id = :cuestionarioId")
    List<Pregunta> findByCuestionarioId(@Param("cuestionarioId") Long cuestionarioId);

    @Query("SELECT COUNT(p) FROM Pregunta p JOIN p.cuestionarios c WHERE c.id = :cuestionarioId")
    int countByCuestionarioId(@Param("cuestionarioId") Long cuestionarioId);
}

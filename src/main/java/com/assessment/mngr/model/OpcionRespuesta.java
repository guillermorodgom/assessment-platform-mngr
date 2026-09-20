package com.assessment.mngr.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "opciones_respuesta")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpcionRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(name = "es_correcta", nullable = false)
    @Builder.Default
    private Boolean esCorrecta = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;
}

package com.assessment.mngr.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "respuestas_candidato")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaCandidato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intento_examen_id", nullable = false)
    private IntentoExamen intentoExamen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    @Column(name = "codigo_fuente", columnDefinition = "TEXT")
    private String codigoFuente;

    @Column(length = 20)
    private String lenguaje;

    @Column(name = "opciones_seleccionadas", columnDefinition = "TEXT")
    private String opcionesSeleccionadas;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado_ejecucion", length = 30)
    private ResultadoEjecucion resultadoEjecucion;

    @Column(name = "salida_obtenida", columnDefinition = "TEXT")
    private String salidaObtenida;

    @Column(name = "es_correcta", nullable = false)
    @Builder.Default
    private Boolean esCorrecta = false;

    @Column(name = "puntaje_obtenido", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal puntajeObtenido = BigDecimal.ZERO;
}

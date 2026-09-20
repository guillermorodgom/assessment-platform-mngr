package com.assessment.mngr.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cuestionarios")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tiempo_limite", nullable = false)
    private Integer tiempoLimite;

    @Column(name = "cantidad_preguntas", nullable = false)
    @Builder.Default
    private Integer cantidadPreguntas = 0;

    @Column(name = "max_intentos", nullable = false)
    private Integer maxIntentos;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
        name = "cuestionario_preguntas",
        joinColumns = @JoinColumn(name = "cuestionario_id"),
        inverseJoinColumns = @JoinColumn(name = "pregunta_id")
    )
    @Builder.Default
    private List<Pregunta> preguntas = new ArrayList<>();
}

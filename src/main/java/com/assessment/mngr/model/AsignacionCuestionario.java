package com.assessment.mngr.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "asignaciones_cuestionario",
       uniqueConstraints = @UniqueConstraint(name = "uk_asignacion", columnNames = {"cuestionario_id", "candidato_id"}))
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionCuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuestionario_id", nullable = false)
    private Cuestionario cuestionario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_id", nullable = false)
    private Usuario candidato;

    @Column(name = "disponible_desde", nullable = false)
    private LocalDateTime disponibleDesde;

    @Column(name = "disponible_hasta", nullable = false)
    private LocalDateTime disponibleHasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_por", nullable = false)
    private Usuario asignadoPor;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

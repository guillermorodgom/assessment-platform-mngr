package com.assessment.mngr.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "preguntas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pregunta", nullable = false, length = 30)
    private TipoPregunta tipoPregunta;

    @ElementCollection(targetClass = LenguajeProgramacion.class)
    @CollectionTable(name = "pregunta_lenguajes", joinColumns = @JoinColumn(name = "pregunta_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "lenguaje", length = 20)
    @Builder.Default
    private List<LenguajeProgramacion> lenguajesPermitidos = new ArrayList<>();

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal puntaje = BigDecimal.ONE;

    @ManyToMany(mappedBy = "preguntas")
    @Builder.Default
    private List<Cuestionario> cuestionarios = new ArrayList<>();

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OpcionRespuesta> opciones = new ArrayList<>();

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CasoDePrueba> casosDePrueba = new ArrayList<>();
}

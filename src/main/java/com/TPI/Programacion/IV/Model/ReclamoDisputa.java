package com.TPI.Programacion.IV.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reclamo_disputa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReclamoDisputa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "motivo", nullable = false, length = 150)
    private String motivo;

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "resolucion_administrativa", length = 1000)
    private String resolucionAdministrativa;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Subasta_id", nullable = false)
    private Subasta subasta;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "usuario_demandante_id", nullable = false)
    private Usuario usuarioDemandante;

}
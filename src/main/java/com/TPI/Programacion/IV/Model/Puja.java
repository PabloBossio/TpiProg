package com.TPI.Programacion.IV.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.web.WebProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Puja")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Puja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_puja", nullable = false)
    private LocalDateTime fechaPuja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subasta_id", nullable = false)
    private Subasta subasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario us;

}
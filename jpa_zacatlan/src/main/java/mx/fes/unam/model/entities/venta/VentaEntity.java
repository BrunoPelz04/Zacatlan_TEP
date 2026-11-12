package mx.fes.unam.model.entities.venta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.fes.unam.model.entities.jornada.JornadaEntity;
import mx.fes.unam.model.entities.producto.ProductoEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
public class VentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ventaId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private ProductoEntity producto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "jornada_id")
    private JornadaEntity jornada;

    private LocalDateTime fecha;
    private BigDecimal precioFinal;      // total cobrado
    private BigDecimal cantidad;
}
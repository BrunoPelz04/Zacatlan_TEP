package mx.fes.unam.model.entities.producto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productoId;

    @Column(nullable = false, length = 50)
    private String nombre;

    private BigDecimal precioEstimado;
    private BigDecimal cantidad;
    private String unidadMedida;
}
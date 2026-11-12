package mx.fes.unam.model.entities.producto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.fes.unam.model.entities.usuario.UsuarioEntity;

import java.math.BigDecimal;

//Cada producto pertenece a UN solo vendedor (producto.usuario_id)
@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
public class ProductoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productoId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;          // vendedor dueño del producto

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioEstimado;      // precio sugerido

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad;            // presentación base (1 kg, 30 pza...)

    @Column(nullable = false, length = 20)
    private String unidadMedida;
}

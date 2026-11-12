package mx.fes.unam.model.entities.historial;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.fes.unam.model.entities.usuario.UsuarioEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial")
@Data
@NoArgsConstructor
public class HistorialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer historialId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;

    private String descripcion;
    private LocalDateTime fecha;
    private String accion;
}
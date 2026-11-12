package mx.fes.unam.model.entities.jornada;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.fes.unam.model.entities.usuario.UsuarioEntity;

import java.time.LocalDateTime;

//Turno de trabajo de un vendedor (fecha_fin NULL = jornada abierta)
@Entity
@Table(name = "jornada")
@Data
@NoArgsConstructor
public class JornadaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer jornadaId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
}

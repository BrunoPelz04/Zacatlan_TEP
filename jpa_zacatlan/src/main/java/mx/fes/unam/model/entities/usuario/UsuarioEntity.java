package mx.fes.unam.model.entities.usuario;

import jakarta.persistence.*;
import lombok.*;
import mx.fes.unam.model.entities.bloque.BloqueEntity;
import mx.fes.unam.model.entities.tipouser.TipoUserEntity;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
public class UsuarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer usuarioId;

    @ManyToOne                          // puede ser null (admins generales)
    @JoinColumn(name = "bloque_id")
    private BloqueEntity bloque;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_user_id")
    private TipoUserEntity tipoUser;

    private String nombres;
    private String apelliPa;
    private Boolean activo;
    private String correo;
    private String passwordHash;
}
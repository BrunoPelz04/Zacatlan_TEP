package mx.fes.unam.model.entities.tipouser;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "tipo_user")
@Data
@NoArgsConstructor
public class TipoUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tipoUserId;

    @Column(nullable = false, length = 30)
    private String nombre;
}
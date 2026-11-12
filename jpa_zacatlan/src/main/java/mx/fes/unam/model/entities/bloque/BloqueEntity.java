package mx.fes.unam.model.entities.bloque;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bloque")
@Data
@NoArgsConstructor
public class BloqueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bloqueId;

    @Column(nullable = false, length = 50)
    private String nombre;
}
package mx.fes.unam.model.sesion;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class SesionUsuario implements Serializable {
    private Integer usuarioId;
    private String nombre;
    private String rol;
    private boolean adminDeBloque;
    private String bloque;
}
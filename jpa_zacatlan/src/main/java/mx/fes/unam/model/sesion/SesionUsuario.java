package mx.fes.unam.model.sesion;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class SesionUsuario implements Serializable {
    private String nombre;
    private String rol;              // "ADMIN" o "VENDEDOR"
    private boolean adminDeBloque;
    private String bloque;           // nombre del bloque, o null
}
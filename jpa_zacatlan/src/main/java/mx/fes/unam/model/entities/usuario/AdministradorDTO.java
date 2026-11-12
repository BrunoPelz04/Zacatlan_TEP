package mx.fes.unam.model.entities.usuario;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdministradorDTO {

    // Campos del formulario
    private Integer id;
    private String nombres;
    private String apelliPa;
    private String apelliMa;
    private String email;
    private Integer bloqueId;      // null = administrador general
    private String password;

    // Solo para mostrar en las plantillas
    private String nombre;         // nombre completo
    private boolean root;          // true = Super admin
    private String tipoTexto;      // Root / General / Bloque X
}
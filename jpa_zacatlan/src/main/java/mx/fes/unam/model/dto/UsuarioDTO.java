package mx.fes.unam.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {
    Integer id;

    //Validaciones del formulario
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre admite hasta 50 caracteres")
    String nombres;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 50, message = "El apellido paterno admite hasta 50 caracteres")
    String apelliPa;

    @Size(max = 50, message = "El apellido materno admite hasta 50 caracteres")
    String apelliMa;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 50, message = "El correo admite hasta 50 caracteres")
    String email;

    @Pattern(regexp = "^$|^\\d{10}$", message = "El teléfono debe tener 10 dígitos")
    String telefono;

    Integer bloqueId;

    @Size(max = 72, message = "La contraseña admite hasta 72 caracteres")
    String password;

    //Solo para mostrar en las tablas
    String bloqueNombre;
    boolean activo = true;

    //Nombre completo para las tablas
    public String getNombre() {
        StringBuilder sb = new StringBuilder(nombres == null ? "" : nombres);
        if (apelliPa != null && !apelliPa.isBlank()) sb.append(' ').append(apelliPa);
        if (apelliMa != null && !apelliMa.isBlank()) sb.append(' ').append(apelliMa);
        return sb.toString().trim();
    }
}

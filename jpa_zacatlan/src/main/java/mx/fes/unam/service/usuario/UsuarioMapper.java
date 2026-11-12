package mx.fes.unam.service.usuario;

import mx.fes.unam.model.dto.UsuarioDTO;
import mx.fes.unam.model.entities.usuario.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {
    //DTO a Entity (usuario nuevo)
    public UsuarioEntity toEntity(UsuarioDTO dto) {
        UsuarioEntity usuario = new UsuarioEntity();
        if (dto != null) {
            copiarDatos(dto, usuario);
        }
        return usuario;
    }

    //Copia los datos capturados en el formulario a una entidad (nueva o existente).
    //No toca bloque, tipo, activo ni contraseña: eso lo decide el service.
    public void copiarDatos(UsuarioDTO dto, UsuarioEntity usuario) {
        usuario.setNombres(dto.getNombres().trim());
        usuario.setApelliPa(dto.getApelliPa().trim());
        usuario.setApelliMa(vacioANull(dto.getApelliMa()));
        usuario.setCorreo(dto.getEmail().trim().toLowerCase());
        usuario.setTelefono(vacioANull(dto.getTelefono()));
    }

    //Entity a DTO (nunca incluye la contraseña)
    public UsuarioDTO toDTO(UsuarioEntity usuario) {
        UsuarioDTO dto = null;
        if (usuario != null) {
            dto = new UsuarioDTO();
            dto.setId(usuario.getUsuarioId());
            dto.setNombres(usuario.getNombres());
            dto.setApelliPa(usuario.getApelliPa());
            dto.setApelliMa(usuario.getApelliMa());
            dto.setEmail(usuario.getCorreo());
            dto.setTelefono(usuario.getTelefono());
            dto.setActivo(Boolean.TRUE.equals(usuario.getActivo()));
            if (usuario.getBloque() != null) {
                dto.setBloqueId(usuario.getBloque().getBloqueId());
                dto.setBloqueNombre(usuario.getBloque().getNombre());
            }
        }
        return dto;
    }

    private String vacioANull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}

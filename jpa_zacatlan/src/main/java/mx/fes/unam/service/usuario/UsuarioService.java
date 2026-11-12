package mx.fes.unam.service.usuario;

import mx.fes.unam.model.entities.bloque.BloqueEntity;
import mx.fes.unam.model.dto.UsuarioDTO;
import mx.fes.unam.model.entities.usuario.UsuarioEntity;
import mx.fes.unam.repository.BloqueRepository;
import mx.fes.unam.repository.TipoUserRepository;
import mx.fes.unam.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    private static final String VENDEDOR = "Vendedor";

    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final BloqueRepository bloqueRepository;
    private final TipoUserRepository tipoUserRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioMapper usuarioMapper,
                          UsuarioRepository usuarioRepository,
                          BloqueRepository bloqueRepository,
                          TipoUserRepository tipoUserRepository) {
        this.usuarioMapper = usuarioMapper;
        this.usuarioRepository = usuarioRepository;
        this.bloqueRepository = bloqueRepository;
        this.tipoUserRepository = tipoUserRepository;
    }

    //Listar vendedores; bloqueId null = todos los bloques
    public List<UsuarioDTO> listarVendedores(Integer bloqueId) {
        List<UsuarioEntity> lista = (bloqueId == null)
                ? usuarioRepository.findByTipoUserNombreOrderByNombresAscApelliPaAsc(VENDEDOR)
                : usuarioRepository.findByTipoUserNombreAndBloqueBloqueIdOrderByNombresAscApelliPaAsc(VENDEDOR, bloqueId);
        return lista.stream().map(u -> usuarioMapper.toDTO(u)).toList();
    }

    //Buscar vendedor por id (null si no existe o no es vendedor)
    public UsuarioDTO obtenerVendedor(Integer id) {
        return usuarioRepository.findById(id)
                .filter(u -> VENDEDOR.equals(u.getTipoUser().getNombre()))
                .map(u -> usuarioMapper.toDTO(u))
                .orElse(null);
    }

    //Alta y modificacion de vendedor (dto.bloqueId ya viene validado por el controller)
    public UsuarioDTO guardarVendedor(UsuarioDTO dto) {
        UsuarioEntity usuario;
        if (dto.getId() == null) {
            usuario = usuarioMapper.toEntity(dto);
            usuario.setActivo(true);
            usuario.setTipoUser(tipoUserRepository.findByNombre(VENDEDOR).orElseThrow());
        } else {
            usuario = usuarioRepository.findById(dto.getId()).orElseThrow();
            usuarioMapper.copiarDatos(dto, usuario);
        }
        usuario.setBloque(bloqueRepository.findById(dto.getBloqueId()).orElseThrow());

        //Contraseña: solo se cambia si se capturó una
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            usuario.setPasswordHash(encoder.encode(dto.getPassword()));
        }
        return usuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    //Activar / desactivar
    public void cambiarEstado(Integer id, boolean activo) {
        UsuarioEntity usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    //Eliminar por id. false si tiene jornadas/ventas (no se puede borrar)
    public boolean eliminarVendedor(Integer id) {
        try {
            usuarioRepository.deleteById(id);
            usuarioRepository.flush();
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    //Regresa true si otro usuario ya tiene ese correo
    public boolean emailEnUso(String email, Integer id) {
        return usuarioRepository.findByCorreo(email.trim().toLowerCase())
                .map(u -> !u.getUsuarioId().equals(id))
                .orElse(false);
    }

    //Bloques para el select del formulario
    public List<BloqueEntity> listarBloques() {
        return bloqueRepository.findAllByOrderByNombreAsc();
    }

    public BloqueEntity obtenerBloque(Integer id) {
        return id == null ? null : bloqueRepository.findById(id).orElse(null);
    }

    public BloqueEntity obtenerBloquePorNombre(String nombre) {
        return bloqueRepository.findByNombre(nombre).orElse(null);
    }
}

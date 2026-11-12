package mx.fes.unam.service.usuario;

import mx.fes.unam.model.entities.bloque.BloqueEntity;
import mx.fes.unam.model.dto.UsuarioDTO;
import mx.fes.unam.model.entities.usuario.AdministradorDTO;
import mx.fes.unam.model.entities.usuario.UsuarioEntity;
import mx.fes.unam.repository.BloqueRepository;
import mx.fes.unam.repository.TipoUserRepository;
import mx.fes.unam.repository.UsuarioRepository;
import mx.fes.unam.service.historial.HistorialService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService {
    private static final String VENDEDOR = "Vendedor";

    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final BloqueRepository bloqueRepository;
    private final TipoUserRepository tipoUserRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final HistorialService historialService;
    private static final String ADMIN = "Admin";
    private static final String SUPER_ADMIN = "Super admin";

    public UsuarioService(UsuarioMapper usuarioMapper,
                          UsuarioRepository usuarioRepository,
                          BloqueRepository bloqueRepository,
                          TipoUserRepository tipoUserRepository,
                          HistorialService historialService) {
        this.usuarioMapper = usuarioMapper;
        this.usuarioRepository = usuarioRepository;
        this.bloqueRepository = bloqueRepository;
        this.tipoUserRepository = tipoUserRepository;
        this.historialService = historialService;
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

    // ---------- Administradores ----------

    @Transactional(readOnly = true)
    public List<AdministradorDTO> listarAdministradores() {
        return usuarioRepository.listarActivosPorTipos(List.of(ADMIN, SUPER_ADMIN))
                .stream().map(this::aAdministradorDTO).toList();
    }

    @Transactional(readOnly = true)
    public AdministradorDTO buscarAdministrador(Integer id) {
        return aAdministradorDTO(obtenerAdmin(id));
    }

    @Transactional
    public void guardarAdministrador(AdministradorDTO dto, Integer autorId) {
        boolean esNuevo = dto.getId() == null;
        String correo = dto.getEmail().trim().toLowerCase();

        boolean correoRepetido = esNuevo
                ? usuarioRepository.existsByCorreo(correo)
                : usuarioRepository.existsByCorreoAndUsuarioIdNot(correo, dto.getId());
        if (correoRepetido) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");
        }

        boolean hayPassword = dto.getPassword() != null && !dto.getPassword().isBlank();
        if (esNuevo && !hayPassword) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (hayPassword && dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        UsuarioEntity u;
        if (esNuevo) {
            u = new UsuarioEntity();
            u.setTipoUser(tipoUserRepository.findByNombre(ADMIN)
                    .orElseThrow(() -> new IllegalStateException("Falta el tipo Admin en la base")));
            u.setActivo(true);
            u.setFechaCreacion(LocalDateTime.now());
        } else {
            u = obtenerAdmin(dto.getId());
        }

        u.setNombres(dto.getNombres().trim());
        u.setApelliPa(dto.getApelliPa().trim());
        u.setApelliMa(dto.getApelliMa() == null || dto.getApelliMa().isBlank()
                ? null : dto.getApelliMa().trim());
        u.setCorreo(correo);

        // El root siempre ve todo, así que no se le asigna bloque
        if (!esRoot(u)) {
            BloqueEntity bloque = null;
            if (dto.getBloqueId() != null) {
                bloque = bloqueRepository.findById(dto.getBloqueId())
                        .orElseThrow(() -> new IllegalArgumentException("El bloque no existe"));
            }
            u.setBloque(bloque);
        }

        if (hayPassword) {
            u.setPasswordHash(encoder.encode(dto.getPassword()));
        }

        usuarioRepository.save(u);

        historialService.registrar(autorId,
                esNuevo ? "crear" : "editar",
                (esNuevo ? "Alta" : "Edición") + " del administrador " + nombreCompletoAdmin(u));
    }

    @Transactional
    public void eliminarAdministrador(Integer id, Integer autorId) {
        UsuarioEntity u = obtenerAdmin(id);

        if (esRoot(u)) {
            throw new IllegalArgumentException("El administrador root no se puede eliminar");
        }
        if (u.getUsuarioId().equals(autorId)) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta");
        }

        u.setActivo(false);
        u.setFechaFin(LocalDateTime.now());
        usuarioRepository.save(u);

        historialService.registrar(autorId, "eliminar",
                "Baja del administrador " + nombreCompletoAdmin(u));
    }

// ---------- Auxiliares de administradores ----------

    private UsuarioEntity obtenerAdmin(Integer id) {
        UsuarioEntity u = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El administrador no existe"));
        String tipo = u.getTipoUser().getNombre();
        boolean esAdmin = tipo.equals(ADMIN) || tipo.equals(SUPER_ADMIN);
        if (!esAdmin || !Boolean.TRUE.equals(u.getActivo())) {
            throw new IllegalArgumentException("El administrador no existe");
        }
        return u;
    }

    private boolean esRoot(UsuarioEntity u) {
        return SUPER_ADMIN.equals(u.getTipoUser().getNombre());
    }

    private String nombreCompletoAdmin(UsuarioEntity u) {
        StringBuilder sb = new StringBuilder(u.getNombres()).append(" ").append(u.getApelliPa());
        if (u.getApelliMa() != null && !u.getApelliMa().isBlank()) {
            sb.append(" ").append(u.getApelliMa());
        }
        return sb.toString();
    }

    private AdministradorDTO aAdministradorDTO(UsuarioEntity u) {
        AdministradorDTO d = new AdministradorDTO();
        d.setId(u.getUsuarioId());
        d.setNombres(u.getNombres());
        d.setApelliPa(u.getApelliPa());
        d.setApelliMa(u.getApelliMa());
        d.setNombre(nombreCompletoAdmin(u));
        d.setEmail(u.getCorreo());
        d.setRoot(esRoot(u));
        d.setBloqueId(u.getBloque() == null ? null : u.getBloque().getBloqueId());
        d.setTipoTexto(esRoot(u) ? "Root"
                : u.getBloque() == null ? "General"
                : "Bloque " + u.getBloque().getNombre());
        return d;
    }
}

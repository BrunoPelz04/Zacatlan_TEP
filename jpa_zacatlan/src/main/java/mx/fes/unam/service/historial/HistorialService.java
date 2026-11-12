package mx.fes.unam.service.historial;

import mx.fes.unam.model.entities.historial.HistorialEntity;
import mx.fes.unam.repository.HistorialRepository;
import mx.fes.unam.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class HistorialService {

    private final HistorialRepository historialRepository;
    private final UsuarioRepository usuarioRepository;

    public HistorialService(HistorialRepository historialRepository,
                            UsuarioRepository usuarioRepository) {
        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrar(Integer usuarioId, String accion, String descripcion) {
        HistorialEntity h = new HistorialEntity();
        h.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        h.setAccion(accion);            // "crear", "editar", "eliminar"
        h.setDescripcion(descripcion);
        h.setFecha(LocalDateTime.now());
        historialRepository.save(h);
    }
}
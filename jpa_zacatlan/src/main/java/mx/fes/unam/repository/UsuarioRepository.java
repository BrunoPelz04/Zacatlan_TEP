package mx.fes.unam.repository;

import mx.fes.unam.model.entities.usuario.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {

    Optional<UsuarioEntity> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    // Para editar: otro usuario (distinto al que edito) ya usa ese correo
    boolean existsByCorreoAndUsuarioIdNot(String correo, Integer usuarioId);

    // Usuarios activos de ciertos tipos (reutilizable para vendedores después)
    @Query("""
           SELECT u FROM UsuarioEntity u
           WHERE u.tipoUser.nombre IN :tipos AND u.activo = true
           ORDER BY u.nombres, u.apelliPa
           """)
    List<UsuarioEntity> listarActivosPorTipos(@Param("tipos") List<String> tipos);
    List<UsuarioEntity> findByTipoUserNombreOrderByNombresAscApelliPaAsc(String nombre);

    List<UsuarioEntity> findByTipoUserNombreAndBloqueBloqueIdOrderByNombresAscApelliPaAsc(
            String nombre, Integer bloqueId);
}
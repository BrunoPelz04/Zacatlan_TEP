package mx.fes.unam.repository;

import mx.fes.unam.model.entities.usuario.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {
    Optional<UsuarioEntity> findByCorreo(String correo);

    // Listados para la pantalla de Vendedores
    List<UsuarioEntity> findByTipoUserNombreOrderByNombresAscApelliPaAsc(String tipo);
    List<UsuarioEntity> findByTipoUserNombreAndBloqueBloqueIdOrderByNombresAscApelliPaAsc(String tipo, Integer bloqueId);
}

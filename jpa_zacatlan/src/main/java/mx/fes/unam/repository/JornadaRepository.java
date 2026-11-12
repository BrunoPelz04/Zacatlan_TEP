package mx.fes.unam.repository;

import mx.fes.unam.model.entities.jornada.JornadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JornadaRepository extends JpaRepository<JornadaEntity, Integer> {
    //Jornada más reciente de un vendedor desde cierta fecha (se usa con el inicio de hoy)
    Optional<JornadaEntity> findFirstByUsuarioUsuarioIdAndFechaInicioGreaterThanEqualOrderByFechaInicioDesc(
            Integer usuarioId, LocalDateTime desde);
}

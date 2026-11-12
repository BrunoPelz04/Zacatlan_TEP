package mx.fes.unam.repository;

import mx.fes.unam.model.entities.historial.HistorialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialRepository extends JpaRepository<HistorialEntity, Integer> {

    // Las 5 acciones más recientes
    List<HistorialEntity> findTop5ByOrderByFechaDesc();
}
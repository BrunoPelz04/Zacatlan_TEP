package mx.fes.unam.repository;

import mx.fes.unam.model.entities.bloque.BloqueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BloqueRepository extends JpaRepository<BloqueEntity, Integer> {
    List<BloqueEntity> findAllByOrderByNombreAsc();
    Optional<BloqueEntity> findByNombre(String nombre);
}

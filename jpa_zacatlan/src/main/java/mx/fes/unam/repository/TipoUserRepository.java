package mx.fes.unam.repository;

import mx.fes.unam.model.entities.tipouser.TipoUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoUserRepository extends JpaRepository<TipoUserEntity, Integer> {
    Optional<TipoUserEntity> findByNombre(String nombre);
}

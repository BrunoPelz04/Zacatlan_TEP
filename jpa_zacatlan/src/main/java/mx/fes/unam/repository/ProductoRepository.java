package mx.fes.unam.repository;

import mx.fes.unam.model.entities.producto.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<ProductoEntity, Integer> {
    //Productos que pertenecen a un vendedor (producto.usuario_id)
    List<ProductoEntity> findByUsuarioUsuarioIdOrderByNombreAsc(Integer usuarioId);
}

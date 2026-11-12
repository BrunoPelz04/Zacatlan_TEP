package mx.fes.unam.repository;

import mx.fes.unam.model.entities.venta.VentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<VentaEntity, Integer> {

    // Cuántas ventas hubo en el rango
    long countByFechaGreaterThanEqualAndFechaLessThan(LocalDateTime inicio, LocalDateTime fin);

    // Suma del dinero vendido en el rango (null si no hay ventas)
    @Query("""
           SELECT SUM(v.precioFinal)
           FROM VentaEntity v
           WHERE v.fecha >= :inicio AND v.fecha < :fin
           """)
    BigDecimal sumaDelDia(@Param("inicio") LocalDateTime inicio,
                          @Param("fin") LocalDateTime fin);

    // Total por bloque: cada fila es [nombreBloque, monto]
    @Query("""
           SELECT b.nombre, SUM(v.precioFinal)
           FROM VentaEntity v
           JOIN v.jornada j
           JOIN j.usuario u
           JOIN u.bloque b
           WHERE v.fecha >= :inicio AND v.fecha < :fin
           GROUP BY b.bloqueId, b.nombre
           ORDER BY SUM(v.precioFinal) DESC
           """)
    List<Object[]> totalPorBloque(@Param("inicio") LocalDateTime inicio,
                                  @Param("fin") LocalDateTime fin);
}
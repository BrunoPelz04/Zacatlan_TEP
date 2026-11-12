package mx.fes.unam.service.venta;

import mx.fes.unam.model.dto.JornadaDTO;
import mx.fes.unam.model.dto.ProductoDTO;
import mx.fes.unam.model.dto.VentaDTO;
import mx.fes.unam.model.entities.jornada.JornadaEntity;
import mx.fes.unam.model.entities.producto.ProductoEntity;
import mx.fes.unam.model.entities.venta.VentaEntity;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
public class VentaMapper {
    //Venta: en la BD se guarda el TOTAL, el precio unitario se calcula
    public VentaDTO toVentaDTO(VentaEntity venta) {
        VentaDTO dto = null;
        if (venta != null) {
            dto = new VentaDTO();
            dto.setId(venta.getVentaId());
            dto.setProductoId(venta.getProducto().getProductoId());
            dto.setProductoNombre(venta.getProducto().getNombre());
            dto.setCantidad(venta.getCantidad());
            dto.setTotal(venta.getPrecioFinal());
            dto.setPrecioUnitario(venta.getPrecioFinal().divide(venta.getCantidad(), 2, RoundingMode.HALF_UP));
            dto.setFecha(venta.getFecha());
        }
        return dto;
    }

    public ProductoDTO toProductoDTO(ProductoEntity producto) {
        ProductoDTO dto = null;
        if (producto != null) {
            dto = new ProductoDTO(
                    producto.getProductoId(),
                    producto.getNombre(),
                    producto.getCantidad().stripTrailingZeros().toPlainString() + " " + producto.getUnidadMedida(),
                    producto.getPrecioEstimado()
            );
        }
        return dto;
    }

    //Sin jornada hoy = DTO vacío (la pantalla lo toma como «sin iniciar»)
    public JornadaDTO toJornadaDTO(JornadaEntity jornada) {
        JornadaDTO dto = new JornadaDTO();
        if (jornada != null) {
            dto.setInicio(jornada.getFechaInicio());
            dto.setFin(jornada.getFechaFin());
        }
        return dto;
    }
}

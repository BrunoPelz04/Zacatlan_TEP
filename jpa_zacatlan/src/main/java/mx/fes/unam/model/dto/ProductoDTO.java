package mx.fes.unam.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoDTO {
    Integer id;
    String nombre;
    String presentacion;    // ej. "1 kg", "30 pza"
    BigDecimal precioBase;  // precio sugerido
}

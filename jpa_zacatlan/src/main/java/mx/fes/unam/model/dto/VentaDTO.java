package mx.fes.unam.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class VentaDTO {
    Integer id;

    //Validaciones del formulario
    @NotNull(message = "Elige un producto")
    Integer productoId;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio admite máximo 2 decimales")
    BigDecimal precioUnitario;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a 0")
    @Digits(integer = 7, fraction = 3, message = "La cantidad admite máximo 3 decimales")
    BigDecimal cantidad;

    //Solo para mostrar en las tablas
    String productoNombre;
    BigDecimal total;
    LocalDateTime fecha;

    public String getHora() {
        return fecha == null ? "" : fecha.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getFechaHoraTexto() {
        return fecha == null ? "" : fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    //1.500 se muestra como 1.5, y 2.000 como 2
    public String getCantidadTexto() {
        return cantidad == null ? "" : cantidad.stripTrailingZeros().toPlainString();
    }
}

package mx.fes.unam.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

//Reporte de ventas de un periodo (se usa para generar el PDF)
@Data
@NoArgsConstructor
public class ReporteDTO {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    String vendedorNombre;
    String bloqueNombre;
    LocalDate desde;
    LocalDate hasta;
    List<VentaDTO> filas;
    int totalVentas;
    BigDecimal totalMonto;

    //"12/11/2026" si es un solo día, "01/11/2026 al 12/11/2026" si son varios
    public String getPeriodoTexto() {
        if (desde == null || hasta == null) return "";
        return desde.equals(hasta)
                ? desde.format(FECHA)
                : desde.format(FECHA) + " al " + hasta.format(FECHA);
    }
}

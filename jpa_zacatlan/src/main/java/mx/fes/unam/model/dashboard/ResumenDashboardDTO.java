package mx.fes.unam.model.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDashboardDTO {

    private int totalVentas;
    private BigDecimal totalMonto;
    private List<BloqueResumen> porBloque;
    private List<ActividadResumen> historial;

    @Data
    @AllArgsConstructor
    public static class BloqueResumen {
        private String nombre;
        private BigDecimal monto;
        private int porcentaje;
    }

    @Data
    @AllArgsConstructor
    public static class ActividadResumen {
        private String autor;
        private String cuando;
        private String descripcion;
    }
}
package mx.fes.unam.service.dashboard;

import mx.fes.unam.model.dashboard.ResumenDashboardDTO;
import mx.fes.unam.model.dashboard.ResumenDashboardDTO.ActividadResumen;
import mx.fes.unam.model.dashboard.ResumenDashboardDTO.BloqueResumen;
import mx.fes.unam.model.entities.historial.HistorialEntity;
import mx.fes.unam.repository.HistorialRepository;
import mx.fes.unam.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final VentaRepository ventaRepository;
    private final HistorialRepository historialRepository;

    public DashboardService(VentaRepository ventaRepository,
                            HistorialRepository historialRepository) {
        this.ventaRepository = ventaRepository;
        this.historialRepository = historialRepository;
    }

    @Transactional(readOnly = true)
    public ResumenDashboardDTO obtenerResumenDelDia() {

        // Rango de hoy: de las 00:00 de hoy a las 00:00 de mañana
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = inicio.plusDays(1);

        int totalVentas = (int) ventaRepository
                .countByFechaGreaterThanEqualAndFechaLessThan(inicio, fin);

        BigDecimal totalMonto = ventaRepository.sumaDelDia(inicio, fin);
        if (totalMonto == null) {
            totalMonto = BigDecimal.ZERO;
        }

        // Ventas por bloque, con su porcentaje del total
        List<BloqueResumen> porBloque = new ArrayList<>();
        for (Object[] fila : ventaRepository.totalPorBloque(inicio, fin)) {
            String nombre = (String) fila[0];
            BigDecimal monto = (BigDecimal) fila[1];
            int porcentaje = totalMonto.signum() == 0 ? 0
                    : monto.multiply(BigDecimal.valueOf(100))
                    .divide(totalMonto, 0, RoundingMode.HALF_UP)
                    .intValue();
            porBloque.add(new BloqueResumen(nombre, monto, porcentaje));
        }

        // Historial reciente
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        List<ActividadResumen> historial = new ArrayList<>();
        for (HistorialEntity h : historialRepository.findTop5ByOrderByFechaDesc()) {
            String autor = h.getUsuario().getNombres() + " " + h.getUsuario().getApelliPa();
            historial.add(new ActividadResumen(
                    autor, h.getFecha().format(formato), h.getDescripcion()));
        }

        return new ResumenDashboardDTO(totalVentas, totalMonto, porBloque, historial);
    }
}
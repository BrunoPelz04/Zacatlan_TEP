package mx.fes.unam.service.venta;

import mx.fes.unam.model.dto.JornadaDTO;
import mx.fes.unam.model.dto.ProductoDTO;
import mx.fes.unam.model.dto.VentaDTO;
import mx.fes.unam.model.entities.jornada.JornadaEntity;
import mx.fes.unam.model.entities.producto.ProductoEntity;
import mx.fes.unam.model.entities.venta.VentaEntity;
import mx.fes.unam.repository.JornadaRepository;
import mx.fes.unam.repository.ProductoRepository;
import mx.fes.unam.repository.UsuarioRepository;
import mx.fes.unam.repository.VentaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VentaService {
    //Máximo que cabe en precio_final DECIMAL(10,2)
    private static final BigDecimal MAX_TOTAL = new BigDecimal("99999999.99");

    private final VentaMapper ventaMapper;
    private final VentaRepository ventaRepository;
    private final JornadaRepository jornadaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public VentaService(VentaMapper ventaMapper,
                        VentaRepository ventaRepository,
                        JornadaRepository jornadaRepository,
                        ProductoRepository productoRepository,
                        UsuarioRepository usuarioRepository) {
        this.ventaMapper = ventaMapper;
        this.ventaRepository = ventaRepository;
        this.jornadaRepository = jornadaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ---------- Jornada del día ----------

    //Estado de las ventas de hoy (sin iniciar / abierta / cerrada)
    public JornadaDTO jornadaDeHoy(Integer usuarioId) {
        return ventaMapper.toJornadaDTO(jornadaHoy(usuarioId).orElse(null));
    }

    //Inicia las ventas del día (solo una vez por día)
    public void iniciarJornada(Integer usuarioId) {
        if (jornadaHoy(usuarioId).isPresent()) {
            throw new IllegalStateException("Ya iniciaste tus ventas de hoy");
        }
        JornadaEntity jornada = new JornadaEntity();
        jornada.setUsuario(usuarioRepository.findById(usuarioId).orElseThrow());
        jornada.setFechaInicio(LocalDateTime.now());
        jornadaRepository.save(jornada);
    }

    //Termina las ventas del día
    public void terminarJornada(Integer usuarioId) {
        JornadaEntity jornada = jornadaAbierta(usuarioId, "No tienes ventas del día abiertas");
        jornada.setFechaFin(LocalDateTime.now());
        jornadaRepository.save(jornada);
    }

    // ---------- Ventas ----------

    //Productos que pertenecen al vendedor (sin productos = lista vacía)
    public List<ProductoDTO> listarProductos(Integer usuarioId) {
        return productoRepository.findByUsuarioUsuarioIdOrderByNombreAsc(usuarioId)
                .stream()
                .map(p -> ventaMapper.toProductoDTO(p))
                .toList();
    }

    //Ventas del vendedor entre dos fechas (ambas incluidas), la más reciente primero
    public List<VentaDTO> listarVentas(Integer usuarioId, LocalDate desde, LocalDate hasta) {
        return ventaRepository.buscar(usuarioId, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay())
                .stream()
                .map(v -> ventaMapper.toVentaDTO(v))
                .toList();
    }

    public BigDecimal totalVentas(List<VentaDTO> ventas) {
        return ventas.stream()
                .map(v -> v.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    //Registra una venta en la jornada abierta de hoy
    public VentaDTO registrarVenta(Integer usuarioId, VentaDTO dto) {
        JornadaEntity jornada = jornadaAbierta(usuarioId,
                "Inicia tus ventas del día para poder registrar ventas");

        //Solo puede vender productos que le pertenecen
        ProductoEntity producto = productoRepository.findById(dto.getProductoId())
                .filter(p -> p.getUsuario().getUsuarioId().equals(usuarioId))
                .orElseThrow(() -> new IllegalStateException("Ese producto no es tuyo"));

        //precio_final guarda el TOTAL: precio unitario x cantidad
        BigDecimal total = dto.getPrecioUnitario().multiply(dto.getCantidad())
                .setScale(2, RoundingMode.HALF_UP);
        if (total.compareTo(MAX_TOTAL) > 0) {
            throw new IllegalStateException("El total de la venta es demasiado grande");
        }

        VentaEntity venta = new VentaEntity();
        venta.setProducto(producto);
        venta.setJornada(jornada);
        venta.setFecha(LocalDateTime.now());
        venta.setPrecioFinal(total);
        venta.setCantidad(dto.getCantidad());
        return ventaMapper.toVentaDTO(ventaRepository.save(venta));
    }

    // ---------- Auxiliares ----------

    private Optional<JornadaEntity> jornadaHoy(Integer usuarioId) {
        return jornadaRepository
                .findFirstByUsuarioUsuarioIdAndFechaInicioGreaterThanEqualOrderByFechaInicioDesc(
                        usuarioId, LocalDate.now().atStartOfDay());
    }

    private JornadaEntity jornadaAbierta(Integer usuarioId, String mensajeError) {
        return jornadaHoy(usuarioId)
                .filter(j -> j.getFechaFin() == null)
                .orElseThrow(() -> new IllegalStateException(mensajeError));
    }
}

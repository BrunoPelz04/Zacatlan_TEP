package mx.fes.unam.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import mx.fes.unam.model.dto.VentaDTO;
import mx.fes.unam.model.sesion.SesionUsuario;
import mx.fes.unam.service.venta.VentaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
//Pantallas del vendedor: Mis ventas y Nueva venta
@RequestMapping("/vendedor")
public class VentaController {
    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    //Mis ventas: del día por defecto, o entre dos fechas
    @GetMapping("/ventas")
    public String misVentas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                            HttpSession session, Model model) {
        SesionUsuario s = sesionVendedor(session);
        if (s == null) return "redirect:/";

        //Fechas por omisión: hoy; si falta una se usa la otra; si vienen al revés se voltean
        if (desde == null && hasta == null) desde = LocalDate.now();
        if (desde == null) desde = hasta;
        if (hasta == null) hasta = desde;
        if (hasta.isBefore(desde)) {
            LocalDate aux = desde;
            desde = hasta;
            hasta = aux;
        }

        List<VentaDTO> ventas = ventaService.listarVentas(s.getUsuarioId(), desde, hasta);
        model.addAttribute("ventas", ventas);
        model.addAttribute("totalDia", ventaService.totalVentas(ventas));
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("varios", !desde.equals(hasta));
        model.addAttribute("activo", "mias");
        return "vendedor/mis-ventas";
    }

    //Nueva venta: estado del día, formulario y ventas de hoy
    @GetMapping("/ventas/nueva")
    public String nuevaVenta(HttpSession session, Model model) {
        SesionUsuario s = sesionVendedor(session);
        if (s == null) return "redirect:/";

        LocalDate hoy = LocalDate.now();
        List<VentaDTO> ventasHoy = ventaService.listarVentas(s.getUsuarioId(), hoy, hoy);
        model.addAttribute("jornada", ventaService.jornadaDeHoy(s.getUsuarioId()));
        model.addAttribute("productos", ventaService.listarProductos(s.getUsuarioId()));
        model.addAttribute("ventas", ventasHoy);
        model.addAttribute("totalDia", ventaService.totalVentas(ventasHoy));
        model.addAttribute("activo", "nueva");
        return "vendedor/nueva-venta";
    }

    //Registrar venta (con validacion)
    @PostMapping("/ventas")
    public String registrar(@Valid @ModelAttribute("venta") VentaDTO venta,
                            BindingResult bindingResult, HttpSession session,
                            RedirectAttributes ra) {
        SesionUsuario s = sesionVendedor(session);
        if (s == null) return "redirect:/";

        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/vendedor/ventas/nueva";
        }
        try {
            ventaService.registrarVenta(s.getUsuarioId(), venta);
            ra.addFlashAttribute("exito", "Venta registrada");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendedor/ventas/nueva";
    }

    @PostMapping("/jornada/iniciar")
    public String iniciarJornada(HttpSession session, RedirectAttributes ra) {
        SesionUsuario s = sesionVendedor(session);
        if (s == null) return "redirect:/";

        try {
            ventaService.iniciarJornada(s.getUsuarioId());
            ra.addFlashAttribute("exito", "Ventas del día iniciadas");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendedor/ventas/nueva";
    }

    @PostMapping("/jornada/terminar")
    public String terminarJornada(HttpSession session, RedirectAttributes ra) {
        SesionUsuario s = sesionVendedor(session);
        if (s == null) return "redirect:/";

        try {
            ventaService.terminarJornada(s.getUsuarioId());
            ra.addFlashAttribute("exito", "Ventas del día terminadas");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendedor/ventas/nueva";
    }

    //Sesion de un vendedor; null si no hay sesion, es admin o es una sesion vieja sin id
    private SesionUsuario sesionVendedor(HttpSession session) {
        Object o = session.getAttribute("sesion");
        if (o instanceof SesionUsuario s && "VENDEDOR".equals(s.getRol()) && s.getUsuarioId() != null) {
            return s;
        }
        return null;
    }
}

package mx.fes.unam.controller;

import jakarta.servlet.http.HttpSession;
import mx.fes.unam.model.entities.usuario.AdministradorDTO;
import mx.fes.unam.model.sesion.SesionUsuario;
import mx.fes.unam.service.bloque.BloqueService;
import mx.fes.unam.service.usuario.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/administradores")
public class AdministradorController {

    private final UsuarioService usuarioService;
    private final BloqueService bloqueService;

    public AdministradorController(UsuarioService usuarioService,
                                   BloqueService bloqueService) {
        this.usuarioService = usuarioService;
        this.bloqueService = bloqueService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("administradores", usuarioService.listarAdministradores());
        return "admin/administradores";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("administrador", new AdministradorDTO());
        model.addAttribute("bloques", bloqueService.listar());
        return "admin/administrador-form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("administrador", usuarioService.buscarAdministrador(id));
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/administradores";
        }
        model.addAttribute("bloques", bloqueService.listar());
        return "admin/administrador-form";
    }

    @PostMapping
    public String guardar(@ModelAttribute AdministradorDTO dto,
                          HttpSession session,
                          RedirectAttributes ra,
                          Model model) {
        SesionUsuario sesion = (SesionUsuario) session.getAttribute("sesion");
        try {
            usuarioService.guardarAdministrador(dto, sesion.getUsuarioId());
        } catch (IllegalArgumentException e) {
            // Regresa al formulario conservando lo que escribió
            model.addAttribute("error", e.getMessage());
            model.addAttribute("administrador", dto);
            model.addAttribute("bloques", bloqueService.listar());
            return "admin/administrador-form";
        }
        ra.addFlashAttribute("exito", "Administrador guardado");
        return "redirect:/admin/administradores";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id,
                           HttpSession session,
                           RedirectAttributes ra) {
        SesionUsuario sesion = (SesionUsuario) session.getAttribute("sesion");
        try {
            usuarioService.eliminarAdministrador(id, sesion.getUsuarioId());
            ra.addFlashAttribute("exito", "Administrador eliminado");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/administradores";
    }
}
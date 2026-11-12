package mx.fes.unam.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import mx.fes.unam.model.entities.bloque.BloqueEntity;
import mx.fes.unam.model.dto.UsuarioDTO;
import mx.fes.unam.model.sesion.SesionUsuario;
import mx.fes.unam.service.usuario.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
//CRUD Vendedores (admin general y admin de bloque)
@RequestMapping("/admin/vendedores")
public class UsuarioController {
    //Servicio con la logica del CRUD de usuarios
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    //Listar vendedores (el admin de bloque solo ve los de su bloque)
    @GetMapping
    public String listar(HttpSession session, Model model) {
        SesionUsuario s = sesionAdmin(session);
        if (s == null) return "redirect:/";

        Integer bloqueId = s.isAdminDeBloque() ? bloqueDe(s).getBloqueId() : null;
        model.addAttribute("vendedores", usuarioService.listarVendedores(bloqueId));
        model.addAttribute("activo", "vendedores");
        return "admin/vendedores";
    }

    //Mostrar formulario de alta
    @GetMapping("nuevo")
    public String nuevo(HttpSession session, Model model) {
        SesionUsuario s = sesionAdmin(session);
        if (s == null) return "redirect:/";

        UsuarioDTO dto = new UsuarioDTO();
        if (s.isAdminDeBloque()) dto.setBloqueId(bloqueDe(s).getBloqueId());
        return formulario(dto, s, model);
    }

    //Mostrar formulario para modificar
    @GetMapping("{id}/editar")
    public String editar(@PathVariable Integer id, HttpSession session,
                         Model model, RedirectAttributes ra) {
        SesionUsuario s = sesionAdmin(session);
        if (s == null) return "redirect:/";

        UsuarioDTO dto = vendedorGestionable(id, s);
        if (dto == null) return noEncontrado(ra);
        return formulario(dto, s, model);
    }

    //Guardar (alta o modificacion) con validacion
    @PostMapping
    public String guardar(@Valid @ModelAttribute("vendedor") UsuarioDTO vendedor,
                          BindingResult bindingResult, HttpSession session,
                          Model model, RedirectAttributes ra) {
        SesionUsuario s = sesionAdmin(session);
        if (s == null) return "redirect:/";

        //En edicion, el vendedor debe existir y estar dentro del alcance del admin
        if (vendedor.getId() != null && vendedorGestionable(vendedor.getId(), s) == null) {
            return noEncontrado(ra);
        }

        //El admin de bloque siempre asigna su bloque; el general elige
        if (s.isAdminDeBloque()) {
            vendedor.setBloqueId(bloqueDe(s).getBloqueId());
        } else if (usuarioService.obtenerBloque(vendedor.getBloqueId()) == null) {
            vendedor.setBloqueId(null);
            bindingResult.reject("bloque", "Elige un bloque");
        }

        //Correo no repetido
        if (!bindingResult.hasFieldErrors("email")
                && usuarioService.emailEnUso(vendedor.getEmail(), vendedor.getId())) {
            bindingResult.rejectValue("email", "duplicado", "Ya existe un usuario con ese correo");
        }

        //Contraseña: obligatoria al crear, minimo 6 si se captura
        String pass = vendedor.getPassword();
        boolean hayPass = pass != null && !pass.isEmpty();
        if (!bindingResult.hasFieldErrors("password")) {
            if (vendedor.getId() == null && !hayPass) {
                bindingResult.rejectValue("password", "requerida", "La contraseña es obligatoria");
            } else if (hayPass && pass.length() < 6) {
                bindingResult.rejectValue("password", "corta", "La contraseña debe tener al menos 6 caracteres");
            }
        }

        //Si hay errores se regresa al formulario
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return formulario(vendedor, s, model);
        }

        usuarioService.guardarVendedor(vendedor);
        ra.addFlashAttribute("exito", vendedor.getId() == null
                ? "Vendedor creado correctamente" : "Vendedor actualizado correctamente");
        return "redirect:/admin/vendedores";
    }

    //Activar / desactivar
    @PostMapping("{id}/estado")
    public String estado(@PathVariable Integer id, @RequestParam boolean activo,
                         HttpSession session, RedirectAttributes ra) {
        SesionUsuario s = sesionAdmin(session);
        if (s == null) return "redirect:/";
        if (vendedorGestionable(id, s) == null) return noEncontrado(ra);

        usuarioService.cambiarEstado(id, activo);
        ra.addFlashAttribute("exito", activo ? "Vendedor activado" : "Vendedor desactivado");
        return "redirect:/admin/vendedores";
    }

    //Eliminar vendedor
    @PostMapping("{id}/eliminar")
    public String eliminar(@PathVariable Integer id, HttpSession session, RedirectAttributes ra) {
        SesionUsuario s = sesionAdmin(session);
        if (s == null) return "redirect:/";
        if (vendedorGestionable(id, s) == null) return noEncontrado(ra);

        if (usuarioService.eliminarVendedor(id)) {
            ra.addFlashAttribute("exito", "Vendedor eliminado");
        } else {
            ra.addFlashAttribute("error",
                    "No se puede eliminar: el vendedor ya tiene jornadas o ventas registradas. Desactívalo en su lugar.");
        }
        return "redirect:/admin/vendedores";
    }

    // ---------- Auxiliares ----------

    //Sesion de un administrador; null si no hay sesion o es vendedor
    private SesionUsuario sesionAdmin(HttpSession session) {
        Object o = session.getAttribute("sesion");
        if (o instanceof SesionUsuario s && "ADMIN".equals(s.getRol())) return s;
        return null;
    }

    //Bloque del admin de bloque (la sesion solo guarda el nombre, que es unico)
    private BloqueEntity bloqueDe(SesionUsuario s) {
        return usuarioService.obtenerBloquePorNombre(s.getBloque());
    }

    //Vendedor existente que este admin puede gestionar; null si no
    private UsuarioDTO vendedorGestionable(Integer id, SesionUsuario s) {
        UsuarioDTO dto = usuarioService.obtenerVendedor(id);
        if (dto != null && s.isAdminDeBloque() && !s.getBloque().equals(dto.getBloqueNombre())) {
            return null;
        }
        return dto;
    }

    private String formulario(UsuarioDTO dto, SesionUsuario s, Model model) {
        List<BloqueEntity> bloques = s.isAdminDeBloque()
                ? List.of(bloqueDe(s))
                : usuarioService.listarBloques();
        model.addAttribute("vendedor", dto);
        model.addAttribute("bloques", bloques);
        model.addAttribute("activo", "vendedores");
        return "admin/vendedor-form";
    }

    private String noEncontrado(RedirectAttributes ra) {
        ra.addFlashAttribute("error", "No se encontró el vendedor solicitado");
        return "redirect:/admin/vendedores";
    }
}

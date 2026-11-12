package mx.fes.unam.controller;

import jakarta.servlet.http.HttpSession;
import mx.fes.unam.model.entities.usuario.UsuarioEntity;
import mx.fes.unam.repository.UsuarioRepository;
import mx.fes.unam.model.sesion.SesionUsuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class InicioController {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public InicioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String inicio() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        Optional<UsuarioEntity> opt = usuarioRepository.findByCorreo(email);

        // Mismo mensaje si el correo no existe o la contraseña es incorrecta
        if (opt.isEmpty() || !encoder.matches(password, opt.get().getPasswordHash())) {
            model.addAttribute("error", "Correo o contraseña incorrectos");
            model.addAttribute("email", email);
            return "login";
        }

        UsuarioEntity u = opt.get();

        if (!Boolean.TRUE.equals(u.getActivo())) {
            model.addAttribute("error", "Tu cuenta está desactivada");
            model.addAttribute("email", email);
            return "login";
        }

        String tipo = u.getTipoUser().getNombre();
        boolean esVendedor = tipo.equals("Vendedor");
        String rol = esVendedor ? "VENDEDOR" : "ADMIN";   // Admin y Super admin
        boolean adminDeBloque = !esVendedor && u.getBloque() != null;
        String bloque = u.getBloque() != null ? u.getBloque().getNombre() : null;

        session.setAttribute("sesion",
                new SesionUsuario(u.getNombres(), rol, adminDeBloque, bloque, u.getUsuarioId()));

        // Redirección según el tipo de usuario
        return esVendedor ? "redirect:/vendedor/ventas" : "redirect:/admin";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
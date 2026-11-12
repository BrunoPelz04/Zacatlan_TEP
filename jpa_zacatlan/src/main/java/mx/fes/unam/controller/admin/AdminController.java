package mx.fes.unam.controller.admin;

import mx.fes.unam.service.dashboard.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DashboardService dashboardService;

    public AdminController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String dashboard(Model model) {

        String fechaTexto = LocalDate.now().format(
                DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",
                        Locale.forLanguageTag("es-MX")));

        model.addAttribute("fechaTexto", fechaTexto);
        model.addAttribute("resumen", dashboardService.obtenerResumenDelDia());

        return "admin/dashboard";
    }
}
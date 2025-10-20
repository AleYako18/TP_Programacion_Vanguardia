package com.gestion_reservas.abm.controller;

import com.gestion_reservas.abm.model.Articulo;
import com.gestion_reservas.abm.model.Rol;
import com.gestion_reservas.abm.model.Sala;
import com.gestion_reservas.abm.model.Usuario;
import com.gestion_reservas.abm.service.ArticuloService;
import com.gestion_reservas.abm.service.ReservaService;
import com.gestion_reservas.abm.service.SalaService;
import com.gestion_reservas.abm.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private SalaService salaService;

    @Autowired
    private ArticuloService articuloService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ReservaService reservaService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        model.addAttribute("nombre", session.getAttribute("nombre"));
        return "admin-dashboard";
    }

    // --- Gestión de Usuarios ---

    @GetMapping("/usuarios")
    public String manageUsuarios(HttpSession session, Model model) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        model.addAttribute("usuarios", usuarioService.findAll());
        return "admin-usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String showEditUsuarioForm(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        usuarioService.findById(id).ifPresent(usuario -> model.addAttribute("usuario", usuario));
        return "admin-usuario-form";
    }

    @PostMapping("/usuarios/guardar")
    public String saveUsuario(@ModelAttribute Usuario usuario, HttpSession session) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        usuarioService.findById(usuario.getId()).ifPresent(existingUser -> {
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                usuario.setPassword(existingUser.getPassword());
            }
        });
        usuarioService.updateUser(usuario);
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String deleteUsuario(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        Long loggedInUserId = (Long) session.getAttribute("userId");
        if (!id.equals(loggedInUserId)) {
            usuarioService.deleteById(id);
        }
        return "redirect:/admin/usuarios";
    }

    // --- Gestión de Salas ---

    @GetMapping("/salas")
    public String manageSalas(HttpSession session, Model model) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        model.addAttribute("salas", salaService.findAll());
        return "admin-salas";
    }

    @GetMapping("/salas/nueva")
    public String showAddSalaForm(HttpSession session, Model model) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        model.addAttribute("sala", new Sala());
        return "admin-sala-form";
    }

    @GetMapping("/salas/editar/{id}")
    public String showEditSalaForm(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        salaService.findById(id).ifPresent(sala -> model.addAttribute("sala", sala));
        return "admin-sala-form";
    }

    @PostMapping("/salas/guardar")
    public String saveSala(@ModelAttribute Sala sala, HttpSession session) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        salaService.save(sala);
        return "redirect:/admin/salas";
    }

    @GetMapping("/salas/eliminar/{id}")
    public String deleteSala(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        salaService.deleteById(id);
        return "redirect:/admin/salas";
    }

    // --- Gestión de Artículos ---

    @GetMapping("/articulos")
    public String manageArticulos(HttpSession session, Model model) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        model.addAttribute("articulos", articuloService.findAll());
        return "admin-articulos";
    }

    @GetMapping("/articulos/nuevo")
    public String showAddArticuloForm(HttpSession session, Model model) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        model.addAttribute("articulo", new Articulo());
        return "admin-articulo-form";
    }

    @GetMapping("/articulos/editar/{id}")
    public String showEditArticuloForm(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        articuloService.findById(id).ifPresent(articulo -> model.addAttribute("articulo", articulo));
        return "admin-articulo-form";
    }

    @PostMapping("/articulos/guardar")
    public String saveArticulo(@ModelAttribute Articulo articulo, HttpSession session) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        articuloService.save(articulo);
        return "redirect:/admin/articulos";
    }

    @GetMapping("/articulos/eliminar/{id}")
    public String deleteArticulo(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        articuloService.deleteById(id);
        return "redirect:/admin/articulos";
    }

    // --- Gestión de Reservas ---

    @GetMapping("/reservas")
    public String manageReservas(HttpSession session, Model model) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        model.addAttribute("reservas", reservaService.findAll());
        return "admin-reservas";
    }

    @GetMapping("/reservas/cancelar/{id}")
    public String cancelReserva(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("rol") != Rol.ADMINISTRADOR) return "redirect:/login";
        reservaService.deleteById(id);
        return "redirect:/admin/reservas";
    }
}

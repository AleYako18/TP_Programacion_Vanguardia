package com.gestion_reservas.abm.controller;

import com.gestion_reservas.abm.model.Rol;
import com.gestion_reservas.abm.model.Usuario;
import com.gestion_reservas.abm.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, Model model, HttpSession session) {
        if (usuarioService.checkLogin(username, password)) {
            Optional<Usuario> usuarioOpt = usuarioService.findByUsername(username);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                session.setAttribute("userId", usuario.getId());
                session.setAttribute("nombre", usuario.getNombre());
                session.setAttribute("rol", usuario.getRol());

                if (usuario.getRol() == Rol.ADMINISTRADOR) {
                    return "redirect:/admin/dashboard";
                } else {
                    return "redirect:/dashboard";
                }
            }
        }
        model.addAttribute("error", "Credenciales inválidas");
        return "login";
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Rol rol = (Rol) session.getAttribute("rol");
        if (rol == Rol.ADMINISTRADOR) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("nombre", session.getAttribute("nombre"));
        return "dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam String nombre,
                                  @RequestParam String apellido,
                                  @RequestParam String fechaNacimiento,
                                  @RequestParam String username,
                                  @RequestParam String password,
                                  RedirectAttributes redirectAttributes) {
        if (usuarioService.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "El correo electrónico ya está registrado.");
            return "redirect:/register";
        }
        Usuario newUser = new Usuario();
        newUser.setNombre(nombre);
        newUser.setApellido(apellido);
        newUser.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRol(Rol.ESTANDAR);
        usuarioService.createUser(newUser);
        redirectAttributes.addFlashAttribute("success", "¡Registro exitoso! Ahora puedes iniciar sesión.");
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String showProfilePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        Optional<Usuario> usuarioOpt = usuarioService.findById(userId);
        if (usuarioOpt.isPresent()) {
            model.addAttribute("usuario", usuarioOpt.get());
            return "edit-profile";
        } else {
            session.invalidate();
            return "redirect:/login";
        }
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String nombre,
                                @RequestParam String apellido,
                                @RequestParam String username,
                                @RequestParam(required = false) String password,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<Usuario> existingUserOpt = usuarioService.findById(userId);
        if (!existingUserOpt.isPresent()) {
            session.invalidate();
            return "redirect:/login";
        }

        Usuario existingUser = existingUserOpt.get();

        Optional<Usuario> userWithSameUsername = usuarioService.findByUsername(username);
        if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(userId)) {
            redirectAttributes.addFlashAttribute("error", "El correo electrónico ya está en uso por otra cuenta.");
            return "redirect:/profile";
        }

        existingUser.setNombre(nombre);
        existingUser.setApellido(apellido);
        // La fecha de nacimiento no se actualiza
        existingUser.setUsername(username);
        if (password != null && !password.isEmpty()) {
            existingUser.setPassword(password);
        }

        usuarioService.updateUser(existingUser);
        session.setAttribute("nombre", existingUser.getNombre());
        redirectAttributes.addFlashAttribute("success", "¡Datos actualizados exitosamente!");
        return "redirect:/profile";
    }
}

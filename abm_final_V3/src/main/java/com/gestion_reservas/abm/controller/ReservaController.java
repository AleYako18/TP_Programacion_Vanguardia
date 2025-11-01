package com.gestion_reservas.abm.controller;

import com.gestion_reservas.abm.model.Articulo;
import com.gestion_reservas.abm.model.Reserva;
import com.gestion_reservas.abm.model.Sala;
import com.gestion_reservas.abm.model.Usuario;
import com.gestion_reservas.abm.service.ArticuloService;
import com.gestion_reservas.abm.service.ReservaService;
import com.gestion_reservas.abm.service.SalaService;
import com.gestion_reservas.abm.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private SalaService salaService;

    @Autowired
    private ArticuloService articuloService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/reservas/nueva")
    public String showNuevaReservaForm(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        model.addAttribute("salas", salaService.findAll());
        model.addAttribute("articulos", articuloService.findAll());
        return "nueva-reserva";
    }

    // --- API Endpoints for Frontend --- 

    @GetMapping("/api/reservas/horarios-ocupados")
    @ResponseBody
    public ResponseEntity<List<Integer>> getHorariosOcupados(
            @RequestParam Long salaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<Integer> horariosOcupados = reservaService.getHorariosOcupados(salaId, fecha);
        return ResponseEntity.ok(horariosOcupados);
    }

    @GetMapping("/api/reservas/articulos-ocupados")
    @ResponseBody
    public ResponseEntity<List<Long>> getArticulosOcupados(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam int hora) {
        LocalDateTime fechaHoraInicio = LocalDateTime.of(fecha, LocalTime.of(hora, 0));
        LocalDateTime fechaHoraFin = fechaHoraInicio.plusHours(1);
        List<Long> ocupadosIds = reservaService.getOcupadosArticuloIds(fechaHoraInicio, fechaHoraFin);
        return ResponseEntity.ok(ocupadosIds);
    }

    // --- Reserva Creation and Management ---

    @PostMapping("/reservas/crear")
    public String crearReserva(@RequestParam Long salaId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                               @RequestParam int hora,
                               @RequestParam(required = false) List<Long> articulosIds,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Optional<Usuario> usuarioOpt = usuarioService.findById(userId);
        Optional<Sala> salaOpt = salaService.findById(salaId);

        if (!usuarioOpt.isPresent() || !salaOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Usuario o Sala no encontrados.");
            return "redirect:/reservas/nueva";
        }

        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setUsuario(usuarioOpt.get());
        nuevaReserva.setSala(salaOpt.get());
        nuevaReserva.setFechaHoraInicio(LocalDateTime.of(fecha, LocalTime.of(hora, 0)));
        nuevaReserva.setFechaHoraFin(nuevaReserva.getFechaHoraInicio().plusHours(1));

        if (articulosIds != null && !articulosIds.isEmpty()) {
            Set<Articulo> articulosSeleccionados = articuloService.findAllByIds(articulosIds);
            nuevaReserva.setArticulos(articulosSeleccionados);
        }

        try {
            reservaService.crearReserva(nuevaReserva);
            redirectAttributes.addFlashAttribute("success", "¡Reserva creada exitosamente!");
            return "redirect:/dashboard";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reservas/nueva";
        }
    }

    @GetMapping("/mis-reservas")
    public String showMisReservas(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        List<Reserva> reservas = reservaService.findByUsuarioId(userId);
        model.addAttribute("reservas", reservas);
        return "mis-reservas";
    }

    @DeleteMapping("/reservas/cancelar-propia/{id}")
    public String cancelOwnReserva(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Optional<Reserva> reservaOpt = reservaService.findById(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            if (reserva.getUsuario().getId().equals(userId)) {
                reservaService.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "¡Reserva cancelada exitosamente!");
            } else {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para cancelar esta reserva.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "La reserva no fue encontrada.");
        }
        return "redirect:/mis-reservas";
    }
}

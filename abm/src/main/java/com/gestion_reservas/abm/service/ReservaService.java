package com.gestion_reservas.abm.service;

import com.gestion_reservas.abm.model.Articulo;
import com.gestion_reservas.abm.model.Reserva;
import com.gestion_reservas.abm.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    public boolean isSalaDisponible(Long salaId, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        List<Reserva> reservasSolapadas = reservaRepository.findReservasSolapadas(salaId, fechaHoraInicio, fechaHoraFin);
        return reservasSolapadas.isEmpty();
    }

    private boolean areArticulosDisponibles(List<Long> articuloIds, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        if (articuloIds == null || articuloIds.isEmpty()) {
            return true; // No hay artículos que comprobar
        }
        List<Long> conflictingIds = reservaRepository.findConflictingArticuloIds(articuloIds, fechaHoraInicio, fechaHoraFin);
        return conflictingIds.isEmpty();
    }

    public List<Integer> getHorariosOcupados(Long salaId, LocalDate fecha) {
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(23, 59, 59);

        List<Reserva> reservasDelDia = reservaRepository.findBySalaIdAndFechaHoraInicioBetween(salaId, inicioDelDia, finDelDia);

        return reservasDelDia.stream()
            .map(reserva -> reserva.getFechaHoraInicio().getHour())
            .collect(Collectors.toList());
    }

    public List<Long> getOcupadosArticuloIds(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        return reservaRepository.findAllArticuloIdsInReservasSolapadas(fechaHoraInicio, fechaHoraFin);
    }

    public Reserva crearReserva(Reserva reserva) {
        // 1. Comprobar disponibilidad de la sala
        if (!isSalaDisponible(reserva.getSala().getId(), reserva.getFechaHoraInicio(), reserva.getFechaHoraFin())) {
            throw new IllegalStateException("La sala ya está reservada en este horario.");
        }

        // 2. Comprobar disponibilidad de los artículos
        if (reserva.getArticulos() != null && !reserva.getArticulos().isEmpty()) {
            List<Long> articuloIds = reserva.getArticulos().stream()
                                            .map(Articulo::getId)
                                            .collect(Collectors.toList());
            if (!areArticulosDisponibles(articuloIds, reserva.getFechaHoraInicio(), reserva.getFechaHoraFin())) {
                throw new IllegalStateException("Uno o más de los artículos seleccionados ya no están disponibles en este horario.");
            }
        }

        // 3. Guardar si todo está disponible
        return reservaRepository.save(reserva);
    }

    public List<Reserva> findAll() {
        return reservaRepository.findAll();
    }

    public List<Reserva> findByUsuarioId(Long usuarioId) {
        return reservaRepository.findByUsuarioId(usuarioId);
    }

    public Optional<Reserva> findById(Long id) {
        return reservaRepository.findById(id);
    }

    public void deleteById(Long id) {
        reservaRepository.deleteById(id);
    }
}

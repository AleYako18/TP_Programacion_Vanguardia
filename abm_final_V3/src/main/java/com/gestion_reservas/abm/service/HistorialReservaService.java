package com.gestion_reservas.abm.service;

import com.gestion_reservas.abm.model.Articulo;
import com.gestion_reservas.abm.model.HistorialReserva;
import com.gestion_reservas.abm.model.Reserva;
import com.gestion_reservas.abm.repository.HistorialReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistorialReservaService {

    @Autowired
    private HistorialReservaRepository historialReservaRepository;

    public void guardarHistorial(Reserva reserva) {
        HistorialReserva historial = new HistorialReserva();
        historial.setReservaId(reserva.getId());
        historial.setUsuarioInfo(reserva.getUsuario().getNombre() + " " + reserva.getUsuario().getApellido() + " (" + reserva.getUsuario().getUsername() + ")");
        historial.setSalaInfo(reserva.getSala().getNombre() + " (Capacidad: " + reserva.getSala().getCapacidad() + ")");
        
        String articulosInfo = reserva.getArticulos().stream()
                                    .map(Articulo::getNombre)
                                    .collect(Collectors.joining(", "));
        historial.setArticulosInfo(articulosInfo.isEmpty() ? "Ninguno" : articulosInfo);

        historial.setFechaHoraInicio(reserva.getFechaHoraInicio());
        historial.setFechaHoraFin(reserva.getFechaHoraFin());
        historial.setFechaCreacionHistorial(LocalDateTime.now());

        historialReservaRepository.save(historial);
    }

    public List<HistorialReserva> findHistorial(String usuarioInfo, LocalDate fechaDesde, LocalDate fechaHasta) {
        LocalDateTime fechaDesdeCompleta = (fechaDesde != null) ? fechaDesde.atStartOfDay() : null;
        LocalDateTime fechaHastaCompleta = (fechaHasta != null) ? fechaHasta.atTime(LocalTime.MAX) : null;

        return historialReservaRepository.findFilteredHistorial(usuarioInfo, fechaDesdeCompleta, fechaHastaCompleta);
    }

    public List<HistorialReserva> findAll() {
        return historialReservaRepository.findAll();
    }
}

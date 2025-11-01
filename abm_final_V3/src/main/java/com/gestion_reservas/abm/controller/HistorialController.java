package com.gestion_reservas.abm.controller;

import com.gestion_reservas.abm.model.HistorialReserva;
import com.gestion_reservas.abm.service.HistorialReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/historial")
public class HistorialController {

    @Autowired
    private HistorialReservaService historialReservaService;

    @GetMapping
    public List<HistorialReserva> obtenerTodoElHistorial() {
        return historialReservaService.findAll();
    }
}

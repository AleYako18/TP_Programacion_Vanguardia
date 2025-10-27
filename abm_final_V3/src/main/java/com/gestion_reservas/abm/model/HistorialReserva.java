package com.gestion_reservas.abm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_reservas")
public class HistorialReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reservaId; // ID de la reserva original

    @Column(columnDefinition = "TEXT")
    private String usuarioInfo;

    @Column(columnDefinition = "TEXT")
    private String salaInfo;

    @Column(columnDefinition = "TEXT")
    private String articulosInfo;

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    private LocalDateTime fechaCreacionHistorial;

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }

    public String getUsuarioInfo() {
        return usuarioInfo;
    }

    public void setUsuarioInfo(String usuarioInfo) {
        this.usuarioInfo = usuarioInfo;
    }

    public String getSalaInfo() {
        return salaInfo;
    }

    public void setSalaInfo(String salaInfo) {
        this.salaInfo = salaInfo;
    }

    public String getArticulosInfo() {
        return articulosInfo;
    }

    public void setArticulosInfo(String articulosInfo) {
        this.articulosInfo = articulosInfo;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public LocalDateTime getFechaCreacionHistorial() {
        return fechaCreacionHistorial;
    }

    public void setFechaCreacionHistorial(LocalDateTime fechaCreacionHistorial) {
        this.fechaCreacionHistorial = fechaCreacionHistorial;
    }
}

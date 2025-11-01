package com.gestion_reservas.abm.repository;

import com.gestion_reservas.abm.model.HistorialReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HistorialReservaRepository extends JpaRepository<HistorialReserva, Long> {

    @Query("SELECT hr FROM HistorialReserva hr " +
           "WHERE (:usuarioInfoParam IS NULL OR LOWER(hr.usuarioInfo) LIKE LOWER(CONCAT('%', :usuarioInfoParam, '%'))) " +
           "AND (:fechaDesdeParam IS NULL OR hr.fechaHoraInicio >= :fechaDesdeParam) " +
           "AND (:fechaHastaParam IS NULL OR hr.fechaHoraInicio <= :fechaHastaParam) " +
           "ORDER BY hr.fechaCreacionHistorial DESC")
    List<HistorialReserva> findFilteredHistorial(
            @Param("usuarioInfoParam") String usuarioInfoParam,
            @Param("fechaDesdeParam") LocalDateTime fechaDesdeParam,
            @Param("fechaHastaParam") LocalDateTime fechaHastaParam
    );
}

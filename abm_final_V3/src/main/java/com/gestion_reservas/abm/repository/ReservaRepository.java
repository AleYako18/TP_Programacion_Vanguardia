package com.gestion_reservas.abm.repository;

import com.gestion_reservas.abm.model.Articulo;
import com.gestion_reservas.abm.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("SELECT r FROM Reserva r WHERE r.sala.id = :salaId AND r.fechaHoraInicio < :fechaHoraFin AND r.fechaHoraFin > :fechaHoraInicio")
    List<Reserva> findReservasSolapadas(
        @Param("salaId") Long salaId,
        @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
        @Param("fechaHoraFin") LocalDateTime fechaHoraFin
    );

    List<Reserva> findBySalaIdAndFechaHoraInicioBetween(
        Long salaId,
        LocalDateTime startOfDay,
        LocalDateTime endOfDay
    );

    List<Reserva> findByUsuarioId(Long usuarioId);

    @Query("SELECT DISTINCT art.id FROM Reserva r JOIN r.articulos art WHERE r.fechaHoraInicio < :fechaHoraFin AND r.fechaHoraFin > :fechaHoraInicio AND art.id IN :articuloIds")
    List<Long> findConflictingArticuloIds(
        @Param("articuloIds") List<Long> articuloIds,
        @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
        @Param("fechaHoraFin") LocalDateTime fechaHoraFin
    );

    @Query("SELECT DISTINCT art.id FROM Reserva r JOIN r.articulos art WHERE r.fechaHoraInicio < :fechaHoraFin AND r.fechaHoraFin > :fechaHoraInicio")
    List<Long> findAllArticuloIdsInReservasSolapadas(
        @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
        @Param("fechaHoraFin") LocalDateTime fechaHoraFin
    );

    // Encuentra todas las reservas que contienen un artículo específico
    List<Reserva> findByArticulosContaining(Articulo articulo);
}

package com.gestion_reservas.abm.service;

import com.gestion_reservas.abm.model.Articulo;
import com.gestion_reservas.abm.model.Reserva;
import com.gestion_reservas.abm.model.Sala;
import com.gestion_reservas.abm.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Anotación clave: Habilita el uso de Mockito.
class ReservaServiceTest {

    @Mock // Crea una versión "falsa" del repositorio. No usará la BD.
    private ReservaRepository reservaRepository;

    @Mock // También creamos un mock para el servicio de historial.
    private HistorialReservaService historialReservaService;

    @InjectMocks // Crea una instancia real de ReservaService, pero le inyecta los mocks de arriba.
    private ReservaService reservaService;

    @Test
    void cuandoSalaNoEstaDisponible_crearReservaDebeLanzarIllegalStateException() {
        // 1. Arrange: Preparamos el escenario
        Sala sala = new Sala();
        sala.setId(1L);
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setSala(sala);
        nuevaReserva.setFechaHoraInicio(LocalDateTime.now());
        nuevaReserva.setFechaHoraFin(LocalDateTime.now().plusHours(1));

        // Le decimos al mock qué debe hacer: "Cuando se llame a findReservasSolapadas,
        // devuelve una lista que contiene una reserva (simulando que la sala está ocupada)".
        when(reservaRepository.findReservasSolapadas(any(), any(), any())).thenReturn(List.of(new Reserva()));

        // 2. Act & 3. Assert: Ejecutamos el método y verificamos que lanza la excepción esperada.
        assertThrows(IllegalStateException.class, () -> {
            reservaService.crearReserva(nuevaReserva);
        });

        // Adicional: Verificamos que el método para guardar la reserva NUNCA fue llamado.
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void cuandoArticuloNoEstaDisponible_crearReservaDebeLanzarIllegalStateException() {
        // 1. Arrange: Preparamos el escenario
        Sala sala = new Sala();
        sala.setId(1L);

        Articulo articulo = new Articulo();
        articulo.setId(10L);

        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setSala(sala);
        nuevaReserva.setArticulos(Set.of(articulo)); // Intentamos reservar un artículo
        nuevaReserva.setFechaHoraInicio(LocalDateTime.now());
        nuevaReserva.setFechaHoraFin(LocalDateTime.now().plusHours(1));

        // Le decimos a los mocks cómo comportarse:
        // La sala SÍ está disponible (la consulta de solapamiento de salas devuelve una lista vacía)
        when(reservaRepository.findReservasSolapadas(any(), any(), any())).thenReturn(Collections.emptyList());

        // Pero el artículo NO está disponible (la consulta de conflicto de artículos devuelve una lista con un ID)
        when(reservaRepository.findConflictingArticuloIds(any(), any(), any())).thenReturn(List.of(10L));

        // 2. Act & 3. Assert: Verificamos que se lanza la excepción correcta
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reservaService.crearReserva(nuevaReserva);
        });

        // Opcional pero recomendado: Verificamos que el mensaje de la excepción es el que esperamos
        assertThat(exception.getMessage()).isEqualTo("Uno o más de los artículos seleccionados ya no están disponibles en este horario.");

        // Verificamos que el método para guardar la reserva NUNCA fue llamado
        verify(reservaRepository, never()).save(any(Reserva.class));
    }
}
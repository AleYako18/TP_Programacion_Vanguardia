package com.gestion_reservas.abm.repository;

import com.gestion_reservas.abm.model.Reserva;
import com.gestion_reservas.abm.model.Sala;
import com.gestion_reservas.abm.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// ANOTACIÓN CORREGIDA:
@DataJpaTest(properties = "spring.sql.init.mode=never") // Deshabilita la ejecución de data.sql
class ReservaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager; // Un ayudante para meter datos en la BD de prueba.

    @Autowired
    private ReservaRepository reservaRepository; // El repositorio que queremos probar.

    @Test
    void cuandoHaySolapamiento_findReservasSolapadasDebeDevolverLaReserva() {
        // 1. Arrange: Preparamos el escenario de prueba
        // Como data.sql está deshabilitado, debemos crear TODOS los datos que necesitamos aquí.
        Usuario usuario = new Usuario();
        usuario.setUsername("test@user.com");
        entityManager.persist(usuario); // Guardamos un usuario

        Sala sala = new Sala();
        sala.setNombre("Sala de Test");
        entityManager.persist(sala); // y una sala de prueba

        // Creamos una reserva existente de 10:00 a 11:00
        Reserva reservaExistente = new Reserva();
        reservaExistente.setUsuario(usuario);
        reservaExistente.setSala(sala);
        reservaExistente.setFechaHoraInicio(LocalDateTime.of(2024, 1, 1, 10, 0));
        reservaExistente.setFechaHoraFin(LocalDateTime.of(2024, 1, 1, 11, 0));
        entityManager.persist(reservaExistente);

        // 2. Act: Ejecutamos el método que queremos probar
        // Buscamos una nueva reserva que se solapa, por ejemplo, de 10:30 a 11:30
        List<Reserva> reservasEncontradas = reservaRepository.findReservasSolapadas(
                sala.getId(),
                LocalDateTime.of(2024, 1, 1, 10, 30),
                LocalDateTime.of(2024, 1, 1, 11, 30)
        );

        // 3. Assert: Verificamos que el resultado es el esperado
        assertThat(reservasEncontradas).hasSize(1);
        assertThat(reservasEncontradas.get(0).getId()).isEqualTo(reservaExistente.getId());
    }
}
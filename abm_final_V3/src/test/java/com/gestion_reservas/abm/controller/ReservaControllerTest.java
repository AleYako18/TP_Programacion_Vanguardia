package com.gestion_reservas.abm.controller;

import com.gestion_reservas.abm.model.Reserva;
import com.gestion_reservas.abm.model.Sala;
import com.gestion_reservas.abm.model.Usuario;
import com.gestion_reservas.abm.service.ArticuloService;
import com.gestion_reservas.abm.service.ReservaService;
import com.gestion_reservas.abm.service.SalaService;
import com.gestion_reservas.abm.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Anotación clave: Prueba solo la capa web (el controlador), sin levantar toda la aplicación.
@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc; // Objeto para simular peticiones HTTP (GET, POST, etc.).

    // Usamos @MockBean para poner versiones "falsas" de los servicios en el contexto de Spring.
    @MockBean
    private ReservaService reservaService;
    @MockBean
    private SalaService salaService;
    @MockBean
    private ArticuloService articuloService;
    @MockBean
    private UsuarioService usuarioService;

    @Test
    void cuandoSeCreaReserva_debeRedirigirYMostrarMensajeExito() throws Exception {
        // 1. Arrange: Preparamos el guion para nuestros mocks.
        long userId = 1L;
        long salaId = 1L;

        // Creamos objetos falsos para simular los datos que esperamos encontrar.
        Usuario mockUsuario = new Usuario();
        mockUsuario.setId(userId);
        Sala mockSala = new Sala();
        mockSala.setId(salaId);

        // Le decimos a los servicios mockeados qué deben devolver:
        // "Cuando alguien llame a usuarioService.findById con el id 1, devuelve el usuario mock"
        when(usuarioService.findById(userId)).thenReturn(Optional.of(mockUsuario));

        // "Cuando alguien llame a salaService.findById con el id 1, devuelve la sala mock"
        when(salaService.findById(salaId)).thenReturn(Optional.of(mockSala));

        // "Cuando se llame a crearReserva con cualquier objeto Reserva, simplemente funciona"
        when(reservaService.crearReserva(any(Reserva.class))).thenReturn(new Reserva());


        // 2. Act & 3. Assert: Ejecutamos la petición y verificamos el resultado.
        mockMvc.perform(post("/reservas/crear") // Simula un POST a /reservas/crear
                        .param("salaId", String.valueOf(salaId))
                        .param("fecha", "2025-10-20")
                        .param("hora", "10")
                        .sessionAttr("userId", userId) // Simula que el 'userId' está en la sesión
                )
                .andExpect(status().is3xxRedirection()) // Esperamos que la respuesta sea una redirección (código 302)
                .andExpect(redirectedUrl("/dashboard")) // Verificamos que la URL de redirección es /dashboard
                .andExpect(flash().attribute("success", "¡Reserva creada exitosamente!")); // Verificamos el mensaje flash
    }
}
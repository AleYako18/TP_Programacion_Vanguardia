package com.gestion_reservas.abm.service;

import com.gestion_reservas.abm.model.Articulo;
import com.gestion_reservas.abm.model.Reserva;
import com.gestion_reservas.abm.repository.ArticuloRepository;
import com.gestion_reservas.abm.repository.ReservaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ArticuloService {

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private ReservaRepository reservaRepository; // Inyectar el repositorio de reservas

    public List<Articulo> findAll() {
        return articuloRepository.findAll();
    }

    public Set<Articulo> findAllByIds(List<Long> ids) {
        return new HashSet<>(articuloRepository.findAllById(ids));
    }

    public Optional<Articulo> findById(Long id) {
        return articuloRepository.findById(id);
    }

    public void save(Articulo articulo) {
        articuloRepository.save(articulo);
    }

    @Transactional
    public void deleteById(Long id) {
        Optional<Articulo> articuloOpt = articuloRepository.findById(id);
        if (articuloOpt.isPresent()) {
            Articulo articulo = articuloOpt.get();

            // 1. Encontrar todas las reservas que usan este artículo
            List<Reserva> reservasAfectadas = reservaRepository.findByArticulosContaining(articulo);

            // 2. Desvincular el artículo de cada reserva
            for (Reserva reserva : reservasAfectadas) {
                reserva.getArticulos().remove(articulo);
            }

            // 3. Eliminar el artículo una vez que ya no está vinculado a ninguna reserva
            articuloRepository.delete(articulo);
        }
    }
}

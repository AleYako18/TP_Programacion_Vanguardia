package com.gestion_reservas.abm.repository;

import com.gestion_reservas.abm.model.Sala;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaRepository extends JpaRepository<Sala, Long> {
}

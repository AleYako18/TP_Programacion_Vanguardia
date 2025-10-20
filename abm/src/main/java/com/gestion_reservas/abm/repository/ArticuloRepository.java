package com.gestion_reservas.abm.repository;

import com.gestion_reservas.abm.model.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticuloRepository extends JpaRepository<Articulo, Long> {
}

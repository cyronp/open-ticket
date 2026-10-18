package com.openticket.repository;

import com.openticket.entity.Chamado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    @Override
    @EntityGraph(attributePaths = {"cliente", "categoria", "tecnico"})
    List<Chamado> findAll();
}

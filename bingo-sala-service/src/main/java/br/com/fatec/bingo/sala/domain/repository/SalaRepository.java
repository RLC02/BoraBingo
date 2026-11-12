package br.com.fatec.bingo.sala.domain.repository;

import br.com.fatec.bingo.sala.domain.model.Sala;
import br.com.fatec.bingo.sala.domain.model.StatusSala;
import java.util.List;
import java.util.Optional;

public interface SalaRepository {
    Sala save(Sala entity);
    Optional<Sala> findById(String id);
    List<Sala> findAll();
    long count();
    void deleteAll();
    List<Sala> findByStatus(StatusSala status);
}

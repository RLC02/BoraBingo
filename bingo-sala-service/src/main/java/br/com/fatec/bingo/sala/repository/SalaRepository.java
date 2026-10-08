package br.com.fatec.bingo.sala.repository;

import br.com.fatec.bingo.sala.model.Sala;
import br.com.fatec.bingo.sala.model.StatusSala;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaRepository extends MongoRepository<Sala, String> {
    List<Sala> findByStatus(StatusSala status);
}

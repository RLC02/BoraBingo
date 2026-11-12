package br.com.fatec.bingo.sala.spring.persistence;

import br.com.fatec.bingo.sala.domain.model.StatusSala;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SalaMongoRepository extends MongoRepository<SalaDocument, String> {
    List<SalaDocument> findByStatus(StatusSala status);
}

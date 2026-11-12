package br.com.fatec.bingo.cartela.spring.persistence;

import br.com.fatec.bingo.cartela.domain.model.StatusCompra;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartelaMongoRepository extends MongoRepository<CartelaDocument, String> {
    List<CartelaDocument> findBySalaIdAndUsername(String salaId, String username);
    List<CartelaDocument> findBySalaId(String salaId);
    long countBySalaId(String salaId);
    long countBySalaIdAndStatus(String salaId, StatusCompra status);
}

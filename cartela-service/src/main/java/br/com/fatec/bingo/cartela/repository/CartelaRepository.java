package br.com.fatec.bingo.cartela.repository;

import br.com.fatec.bingo.cartela.model.Cartela;
import br.com.fatec.bingo.cartela.model.StatusCompra;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartelaRepository extends MongoRepository<Cartela, String> {
    List<Cartela> findBySalaIdAndUsername(String salaId, String username);
    List<Cartela> findBySalaId(String salaId);
    long countBySalaId(String salaId);
    long countBySalaIdAndStatus(String salaId, StatusCompra status);
}

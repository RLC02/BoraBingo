package br.com.fatec.bingo.cartela.domain.repository;

import br.com.fatec.bingo.cartela.domain.model.Cartela;
import br.com.fatec.bingo.cartela.domain.model.StatusCompra;
import java.util.List;
import java.util.Optional;

public interface CartelaRepository {
    Cartela save(Cartela entity);
    Optional<Cartela> findById(String id);
    List<Cartela> findBySalaIdAndUsername(String salaId, String username);
    List<Cartela> findBySalaId(String salaId);
    long countBySalaId(String salaId);
    long countBySalaIdAndStatus(String salaId, StatusCompra status);
}

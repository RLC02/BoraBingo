package br.com.fatec.bingo.cartela.spring.persistence;

import br.com.fatec.bingo.cartela.domain.model.Cartela;
import br.com.fatec.bingo.cartela.domain.model.StatusCompra;
import br.com.fatec.bingo.cartela.domain.repository.CartelaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CartelaRepositoryAdapter implements CartelaRepository {

    private final CartelaMongoRepository mongo;

    public CartelaRepositoryAdapter(CartelaMongoRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Cartela save(Cartela entity) {
        return toDomain(mongo.save(toDocument(entity)));
    }

    @Override
    public Optional<Cartela> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Cartela> findBySalaIdAndUsername(String salaId, String username) {
        return mongo.findBySalaIdAndUsername(salaId, username).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Cartela> findBySalaId(String salaId) {
        return mongo.findBySalaId(salaId).stream().map(this::toDomain).toList();
    }

    @Override
    public long countBySalaId(String salaId) {
        return mongo.countBySalaId(salaId);
    }

    @Override
    public long countBySalaIdAndStatus(String salaId, StatusCompra status) {
        return mongo.countBySalaIdAndStatus(salaId, status);
    }

    private Cartela toDomain(CartelaDocument d) {
        Cartela e = new Cartela();
        e.setId(d.getId());
        e.setSalaId(d.getSalaId());
        e.setUserId(d.getUserId());
        e.setUsername(d.getUsername());
        e.setMatrizNumeros(d.getMatrizNumeros());
        e.setMarcacoes(d.getMarcacoes());
        e.setStatus(d.getStatus());
        e.setMotivoRejeicao(d.getMotivoRejeicao());
        e.setCriadoEm(d.getCriadoEm());
        return e;
    }

    private CartelaDocument toDocument(Cartela e) {
        CartelaDocument d = new CartelaDocument();
        d.setId(e.getId());
        d.setSalaId(e.getSalaId());
        d.setUserId(e.getUserId());
        d.setUsername(e.getUsername());
        d.setMatrizNumeros(e.getMatrizNumeros());
        d.setMarcacoes(e.getMarcacoes());
        d.setStatus(e.getStatus());
        d.setMotivoRejeicao(e.getMotivoRejeicao());
        d.setCriadoEm(e.getCriadoEm());
        return d;
    }
}

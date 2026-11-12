package br.com.fatec.bingo.sala.spring.persistence;

import br.com.fatec.bingo.sala.domain.model.Sala;
import br.com.fatec.bingo.sala.domain.model.StatusSala;
import br.com.fatec.bingo.sala.domain.repository.SalaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class SalaRepositoryAdapter implements SalaRepository {

    private final SalaMongoRepository mongo;

    public SalaRepositoryAdapter(SalaMongoRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Sala save(Sala entity) {
        return toDomain(mongo.save(toDocument(entity)));
    }

    @Override
    public Optional<Sala> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Sala> findAll() {
        return mongo.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public long count() {
        return mongo.count();
    }

    @Override
    public void deleteAll() {
        mongo.deleteAll();
    }

    @Override
    public List<Sala> findByStatus(StatusSala status) {
        return mongo.findByStatus(status).stream().map(this::toDomain).toList();
    }

    private Sala toDomain(SalaDocument d) {
        Sala e = new Sala();
        e.setId(d.getId());
        e.setNome(d.getNome());
        e.setDescricao(d.getDescricao());
        e.setPrecoCartela(d.getPrecoCartela());
        e.setMaxCartelas(d.getMaxCartelas());
        e.setCartelasVendidas(d.getCartelasVendidas());
        e.setStatus(d.getStatus());
        e.setNumerosSorteados(d.getNumerosSorteados());
        e.setVencedorUsername(d.getVencedorUsername());
        e.setVencedorCartelaId(d.getVencedorCartelaId());
        e.setCriadoEm(d.getCriadoEm());
        e.setVersion(d.getVersion());
        return e;
    }

    private SalaDocument toDocument(Sala e) {
        SalaDocument d = new SalaDocument();
        d.setId(e.getId());
        d.setNome(e.getNome());
        d.setDescricao(e.getDescricao());
        d.setPrecoCartela(e.getPrecoCartela());
        d.setMaxCartelas(e.getMaxCartelas());
        d.setCartelasVendidas(e.getCartelasVendidas());
        d.setStatus(e.getStatus());
        d.setNumerosSorteados(e.getNumerosSorteados());
        d.setVencedorUsername(e.getVencedorUsername());
        d.setVencedorCartelaId(e.getVencedorCartelaId());
        d.setCriadoEm(e.getCriadoEm());
        d.setVersion(e.getVersion());
        return d;
    }
}

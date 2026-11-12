package br.com.fatec.bingo.auth.spring.persistence;

import br.com.fatec.bingo.auth.domain.model.Role;
import br.com.fatec.bingo.auth.domain.model.User;
import br.com.fatec.bingo.auth.domain.repository.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserMongoRepository mongo;

    public UserRepositoryAdapter(UserMongoRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public User save(User entity) {
        return toDomain(mongo.save(toDocument(entity)));
    }

    @Override
    public Optional<User> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return mongo.findByUsername(username).map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return mongo.existsByUsername(username);
    }

    private User toDomain(UserDocument d) {
        User e = new User();
        e.setId(d.getId());
        e.setUsername(d.getUsername());
        e.setPassword(d.getPassword());
        e.setNome(d.getNome());
        e.setRole(d.getRole());
        e.setSaldo(d.getSaldo());
        e.setCriadoEm(d.getCriadoEm());
        return e;
    }

    private UserDocument toDocument(User e) {
        UserDocument d = new UserDocument();
        d.setId(e.getId());
        d.setUsername(e.getUsername());
        d.setPassword(e.getPassword());
        d.setNome(e.getNome());
        d.setRole(e.getRole());
        d.setSaldo(e.getSaldo());
        d.setCriadoEm(e.getCriadoEm());
        return d;
    }
}

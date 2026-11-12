package br.com.fatec.bingo.auth.domain.repository;

import br.com.fatec.bingo.auth.domain.model.User;
import java.util.Optional;

public interface UserRepository {
    User save(User entity);
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}

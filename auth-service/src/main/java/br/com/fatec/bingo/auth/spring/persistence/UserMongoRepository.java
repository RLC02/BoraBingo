package br.com.fatec.bingo.auth.spring.persistence;

import br.com.fatec.bingo.auth.domain.model.Role;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserMongoRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByUsername(String username);
    boolean existsByUsername(String username);
}

package br.com.fatec.bingo.auth.spring.config;

import br.com.fatec.bingo.auth.domain.model.Role;
import br.com.fatec.bingo.auth.domain.model.User;
import br.com.fatec.bingo.auth.domain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            criarUsuarioSeNaoExistir("jogador", "jogador123", "Jogador Teste", Role.ROLE_USER);
            criarUsuarioSeNaoExistir("admin",   "admin123",   "Administrador", Role.ROLE_ADMIN);
            recarregarSaldoTeste("jogador", 10000.0);
        } catch (Exception e) {
            System.out.println("[DataLoader] Aviso: não foi possível criar usuários padrão agora: " + e.getMessage());
            System.out.println("[DataLoader] Os usuários serão criados na próxima inicialização ou via /api/auth/register");
        }
    }

    private void criarUsuarioSeNaoExistir(String username, String password, String nome, Role role) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .nome(nome)
                    .role(role)
                    .saldo(100.0)
                    .build();
            userRepository.save(user);
            System.out.println("[DataLoader] Usuário criado: " + username + " (" + role + ")");
        } else {
            System.out.println("[DataLoader] Usuário já existe: " + username);
        }
    }

    private void recarregarSaldoTeste(String username, double saldo) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getSaldo() == null || user.getSaldo() < saldo) {
                user.setSaldo(saldo);
                userRepository.save(user);
                System.out.println("[DataLoader] Saldo de " + username + " recarregado para R$ " + saldo);
            }
        });
    }
}

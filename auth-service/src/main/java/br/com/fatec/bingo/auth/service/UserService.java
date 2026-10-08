package br.com.fatec.bingo.auth.service;

import br.com.fatec.bingo.auth.dto.AuthDTOs.*;
import br.com.fatec.bingo.auth.model.Role;
import br.com.fatec.bingo.auth.model.User;
import br.com.fatec.bingo.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username já está em uso.");
        }

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .nome(req.getNome())
                .role(req.getRole() != null ? req.getRole() : Role.ROLE_USER)
                .saldo(100.0)
                .build();

        user = userRepository.save(user);

        return toResponse(user);
    }

    public String login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Credenciais inválidas.");
        }

        return jwtService.generateToken(user.getUsername(), user.getRole().name(), user.getId());
    }

    public UserResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return toResponse(user);
    }

    @Transactional
    public UserResponse debitarSaldo(String username, Double valor) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (user.getSaldo() == null || user.getSaldo() < valor) {
            throw new IllegalStateException("Saldo insuficiente.");
        }

        user.setSaldo(user.getSaldo() - valor);
        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse creditarSaldo(String username, Double valor) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        user.setSaldo((user.getSaldo() != null ? user.getSaldo() : 0.0) + valor);
        user = userRepository.save(user);
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setNome(user.getNome());
        res.setRole(user.getRole());
        res.setSaldo(user.getSaldo());
        return res;
    }
}

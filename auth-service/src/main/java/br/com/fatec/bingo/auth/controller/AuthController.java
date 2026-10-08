package br.com.fatec.bingo.auth.controller;

import br.com.fatec.bingo.auth.dto.AuthDTOs.*;
import br.com.fatec.bingo.auth.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        String token = userService.login(request);
        UserResponse profile = userService.getProfile(request.getUsername());

        // Configuração de Cookie HttpOnly, Secure, SameSite=Lax
        ResponseCookie cookie = ResponseCookie.from("bingo_auth_token", token)
                .httpOnly(true)
                .secure(false) // Permitir localhost em ambiente de desenvolvimento (true em produção HTTPS)
                .path("/")
                .maxAge(86400) // 24h
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of(
                "message", "Login realizado com sucesso",
                "user", profile,
                "token", token
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("bingo_auth_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(Map.of("message", "Logout efetuado com sucesso"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@RequestHeader(value = "X-Auth-Username", required = false) String username) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getProfile(username));
    }

    @PostMapping("/debitar")
    public ResponseEntity<UserResponse> debitar(
            @RequestHeader(value = "X-Auth-Username", required = false) String username,
            @RequestParam("valor") Double valor) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.debitarSaldo(username, valor));
    }

    @PostMapping("/creditar")
    public ResponseEntity<UserResponse> creditar(
            @RequestHeader(value = "X-Auth-Username", required = false) String username,
            @RequestParam("valor") Double valor) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.creditarSaldo(username, valor));
    }
}

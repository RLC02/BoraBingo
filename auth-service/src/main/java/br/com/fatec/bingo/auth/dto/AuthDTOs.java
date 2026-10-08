package br.com.fatec.bingo.auth.dto;

import br.com.fatec.bingo.auth.model.Role;
import jakarta.validation.constraints.NotBlank;

public class AuthDTOs {

    public static class LoginRequest {
        @NotBlank(message = "Username é obrigatório")
        private String username;

        @NotBlank(message = "Senha é obrigatória")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        @NotBlank(message = "Username é obrigatório")
        private String username;

        @NotBlank(message = "Senha é obrigatória")
        private String password;

        @NotBlank(message = "Nome é obrigatório")
        private String nome;

        private Role role;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
    }

    public static class UserResponse {
        private String id;
        private String username;
        private String nome;
        private Role role;
        private Double saldo;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public Double getSaldo() { return saldo; }
        public void setSaldo(Double saldo) { this.saldo = saldo; }
    }
}

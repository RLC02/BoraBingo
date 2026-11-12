package br.com.fatec.bingo.auth.domain.model;

import java.time.LocalDateTime;

public class User {
    private String id;
    private String username;
    private String password;
    private String nome;
    private Role role;
    private Double saldo = 100.0;
    private LocalDateTime criadoEm;

    public User() {
    }

    public User(String id, String username, String password, String nome, Role role, Double saldo, LocalDateTime criadoEm) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.role = role;
        this.saldo = saldo != null ? saldo : 100.0;
        this.criadoEm = criadoEm;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private String id;
        private String username;
        private String password;
        private String nome;
        private Role role;
        private Double saldo = 100.0;
        private LocalDateTime criadoEm;

        public UserBuilder id(String id) { this.id = id; return this; }
        public UserBuilder username(String username) { this.username = username; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder nome(String nome) { this.nome = nome; return this; }
        public UserBuilder role(Role role) { this.role = role; return this; }
        public UserBuilder saldo(Double saldo) { this.saldo = saldo; return this; }
        public UserBuilder criadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; return this; }
        public User build() { return new User(id, username, password, nome, role, saldo, criadoEm); }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Double getSaldo() { return saldo; }
    public void setSaldo(Double saldo) { this.saldo = saldo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}

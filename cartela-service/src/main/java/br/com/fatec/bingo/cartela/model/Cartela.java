package br.com.fatec.bingo.cartela.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "cartelas")
public class Cartela {

    @Id
    private String id;
    private String salaId;
    private String userId;
    private String username;
    private List<List<Integer>> matrizNumeros;
    private List<Integer> marcacoes;
    private StatusCompra status = StatusCompra.PENDENTE;
    private String motivoRejeicao;

    @CreatedDate
    private LocalDateTime criadoEm;

    public Cartela() {}

    public Cartela(String id, String salaId, String userId, String username, List<List<Integer>> matrizNumeros,
                   List<Integer> marcacoes, StatusCompra status, String motivoRejeicao, LocalDateTime criadoEm) {
        this.id = id;
        this.salaId = salaId;
        this.userId = userId;
        this.username = username;
        this.matrizNumeros = matrizNumeros;
        this.marcacoes = marcacoes;
        this.status = status != null ? status : StatusCompra.PENDENTE;
        this.motivoRejeicao = motivoRejeicao;
        this.criadoEm = criadoEm;
    }

    public static CartelaBuilder builder() {
        return new CartelaBuilder();
    }

    public static class CartelaBuilder {
        private String id;
        private String salaId;
        private String userId;
        private String username;
        private List<List<Integer>> matrizNumeros;
        private List<Integer> marcacoes;
        private StatusCompra status = StatusCompra.PENDENTE;
        private String motivoRejeicao;
        private LocalDateTime criadoEm;

        public CartelaBuilder id(String id) { this.id = id; return this; }
        public CartelaBuilder salaId(String salaId) { this.salaId = salaId; return this; }
        public CartelaBuilder userId(String userId) { this.userId = userId; return this; }
        public CartelaBuilder username(String username) { this.username = username; return this; }
        public CartelaBuilder matrizNumeros(List<List<Integer>> matrizNumeros) { this.matrizNumeros = matrizNumeros; return this; }
        public CartelaBuilder marcacoes(List<Integer> marcacoes) { this.marcacoes = marcacoes; return this; }
        public CartelaBuilder status(StatusCompra status) { this.status = status; return this; }
        public CartelaBuilder motivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; return this; }
        public CartelaBuilder criadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; return this; }
        public Cartela build() {
            return new Cartela(id, salaId, userId, username, matrizNumeros, marcacoes, status, motivoRejeicao, criadoEm);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSalaId() { return salaId; }
    public void setSalaId(String salaId) { this.salaId = salaId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<List<Integer>> getMatrizNumeros() { return matrizNumeros; }
    public void setMatrizNumeros(List<List<Integer>> matrizNumeros) { this.matrizNumeros = matrizNumeros; }
    public List<Integer> getMarcacoes() { return marcacoes; }
    public void setMarcacoes(List<Integer> marcacoes) { this.marcacoes = marcacoes; }
    public StatusCompra getStatus() { return status; }
    public void setStatus(StatusCompra status) { this.status = status; }
    public String getMotivoRejeicao() { return motivoRejeicao; }
    public void setMotivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}

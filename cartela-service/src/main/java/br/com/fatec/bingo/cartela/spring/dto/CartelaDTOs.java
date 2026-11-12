package br.com.fatec.bingo.cartela.spring.dto;

import br.com.fatec.bingo.cartela.domain.model.StatusCompra;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class CartelaDTOs {

    public static class SolicitarCompraRequest {
        @NotBlank(message = "ID da sala é obrigatório")
        private String salaId;

        public String getSalaId() { return salaId; }
        public void setSalaId(String salaId) { this.salaId = salaId; }
    }

    public static class CartelaResponse {
        private String id;
        private String salaId;
        private String userId;
        private String username;
        private List<List<Integer>> matrizNumeros;
        private List<Integer> marcacoes;
        private StatusCompra status;
        private String motivoRejeicao;

        public CartelaResponse() {}

        public CartelaResponse(String id, String salaId, String userId, String username, List<List<Integer>> matrizNumeros, List<Integer> marcacoes, StatusCompra status, String motivoRejeicao) {
            this.id = id;
            this.salaId = salaId;
            this.userId = userId;
            this.username = username;
            this.matrizNumeros = matrizNumeros;
            this.marcacoes = marcacoes;
            this.status = status;
            this.motivoRejeicao = motivoRejeicao;
        }

        public static CartelaResponseBuilder builder() {
            return new CartelaResponseBuilder();
        }

        public static class CartelaResponseBuilder {
            private String id;
            private String salaId;
            private String userId;
            private String username;
            private List<List<Integer>> matrizNumeros;
            private List<Integer> marcacoes;
            private StatusCompra status;
            private String motivoRejeicao;

            public CartelaResponseBuilder id(String id) { this.id = id; return this; }
            public CartelaResponseBuilder salaId(String salaId) { this.salaId = salaId; return this; }
            public CartelaResponseBuilder userId(String userId) { this.userId = userId; return this; }
            public CartelaResponseBuilder username(String username) { this.username = username; return this; }
            public CartelaResponseBuilder matrizNumeros(List<List<Integer>> matrizNumeros) { this.matrizNumeros = matrizNumeros; return this; }
            public CartelaResponseBuilder marcacoes(List<Integer> marcacoes) { this.marcacoes = marcacoes; return this; }
            public CartelaResponseBuilder status(StatusCompra status) { this.status = status; return this; }
            public CartelaResponseBuilder motivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; return this; }
            public CartelaResponse build() {
                return new CartelaResponse(id, salaId, userId, username, matrizNumeros, marcacoes, status, motivoRejeicao);
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
    }

    public static class CompraMessage implements Serializable {
        private String cartelaId;
        private String salaId;
        private String userId;
        private String username;
        private StatusCompra status;
        private LocalDateTime timestamp;

        public CompraMessage() {}

        public CompraMessage(String cartelaId, String salaId, String userId, String username, StatusCompra status, LocalDateTime timestamp) {
            this.cartelaId = cartelaId;
            this.salaId = salaId;
            this.userId = userId;
            this.username = username;
            this.status = status;
            this.timestamp = timestamp;
        }

        public static CompraMessageBuilder builder() {
            return new CompraMessageBuilder();
        }

        public static class CompraMessageBuilder {
            private String cartelaId;
            private String salaId;
            private String userId;
            private String username;
            private StatusCompra status;
            private LocalDateTime timestamp;

            public CompraMessageBuilder cartelaId(String cartelaId) { this.cartelaId = cartelaId; return this; }
            public CompraMessageBuilder salaId(String salaId) { this.salaId = salaId; return this; }
            public CompraMessageBuilder userId(String userId) { this.userId = userId; return this; }
            public CompraMessageBuilder username(String username) { this.username = username; return this; }
            public CompraMessageBuilder status(StatusCompra status) { this.status = status; return this; }
            public CompraMessageBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
            public CompraMessage build() {
                return new CompraMessage(cartelaId, salaId, userId, username, status, timestamp);
            }
        }

        public String getCartelaId() { return cartelaId; }
        public void setCartelaId(String cartelaId) { this.cartelaId = cartelaId; }
        public String getSalaId() { return salaId; }
        public void setSalaId(String salaId) { this.salaId = salaId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public StatusCompra getStatus() { return status; }
        public void setStatus(StatusCompra status) { this.status = status; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class SalaInfoDTO {
        private String id;
        private String nome;
        private Double precoCartela;
        private Integer maxCartelas;
        private Integer cartelasVendidas;
        private String status;
        private List<Integer> numerosSorteados;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public Double getPrecoCartela() { return precoCartela; }
        public void setPrecoCartela(Double precoCartela) { this.precoCartela = precoCartela; }
        public Integer getMaxCartelas() { return maxCartelas; }
        public void setMaxCartelas(Integer maxCartelas) { this.maxCartelas = maxCartelas; }
        public Integer getCartelasVendidas() { return cartelasVendidas; }
        public void setCartelasVendidas(Integer cartelasVendidas) { this.cartelasVendidas = cartelasVendidas; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<Integer> getNumerosSorteados() { return numerosSorteados; }
        public void setNumerosSorteados(List<Integer> numerosSorteados) { this.numerosSorteados = numerosSorteados; }
    }

    public static class MarcarNumeroRequest {
        private Integer numero;

        public Integer getNumero() { return numero; }
        public void setNumero(Integer numero) { this.numero = numero; }
    }
}

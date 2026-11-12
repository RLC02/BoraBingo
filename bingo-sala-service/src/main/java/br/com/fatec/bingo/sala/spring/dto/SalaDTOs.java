package br.com.fatec.bingo.sala.spring.dto;

import br.com.fatec.bingo.sala.domain.model.StatusSala;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class SalaDTOs {

    public static class CriarSalaRequest {
        @NotBlank(message = "Nome da sala é obrigatório")
        private String nome;

        private String descricao;

        @NotNull(message = "Preço da cartela é obrigatório")
        @Min(value = 0, message = "Preço não pode ser negativo")
        private Double precoCartela;

        @NotNull(message = "Máximo de cartelas é obrigatório")
        @Min(value = 1, message = "Mínimo de 1 cartela")
        private Integer maxCartelas;

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public Double getPrecoCartela() { return precoCartela; }
        public void setPrecoCartela(Double precoCartela) { this.precoCartela = precoCartela; }
        public Integer getMaxCartelas() { return maxCartelas; }
        public void setMaxCartelas(Integer maxCartelas) { this.maxCartelas = maxCartelas; }
    }

    public static class SalaResponse {
        private String id;
        private String nome;
        private String descricao;
        private Double precoCartela;
        private Integer maxCartelas;
        private Integer cartelasVendidas;
        private StatusSala status;
        private List<Integer> numerosSorteados;
        private String vencedorUsername;
        private String vencedorCartelaId;

        public SalaResponse() {}

        public SalaResponse(String id, String nome, String descricao, Double precoCartela, Integer maxCartelas,
                            Integer cartelasVendidas, StatusSala status, List<Integer> numerosSorteados,
                            String vencedorUsername, String vencedorCartelaId) {
            this.id = id;
            this.nome = nome;
            this.descricao = descricao;
            this.precoCartela = precoCartela;
            this.maxCartelas = maxCartelas;
            this.cartelasVendidas = cartelasVendidas;
            this.status = status;
            this.numerosSorteados = numerosSorteados;
            this.vencedorUsername = vencedorUsername;
            this.vencedorCartelaId = vencedorCartelaId;
        }

        public static SalaResponseBuilder builder() {
            return new SalaResponseBuilder();
        }

        public static class SalaResponseBuilder {
            private String id;
            private String nome;
            private String descricao;
            private Double precoCartela;
            private Integer maxCartelas;
            private Integer cartelasVendidas;
            private StatusSala status;
            private List<Integer> numerosSorteados;
            private String vencedorUsername;
            private String vencedorCartelaId;

            public SalaResponseBuilder id(String id) { this.id = id; return this; }
            public SalaResponseBuilder nome(String nome) { this.nome = nome; return this; }
            public SalaResponseBuilder descricao(String descricao) { this.descricao = descricao; return this; }
            public SalaResponseBuilder precoCartela(Double precoCartela) { this.precoCartela = precoCartela; return this; }
            public SalaResponseBuilder maxCartelas(Integer maxCartelas) { this.maxCartelas = maxCartelas; return this; }
            public SalaResponseBuilder cartelasVendidas(Integer cartelasVendidas) { this.cartelasVendidas = cartelasVendidas; return this; }
            public SalaResponseBuilder status(StatusSala status) { this.status = status; return this; }
            public SalaResponseBuilder numerosSorteados(List<Integer> numerosSorteados) { this.numerosSorteados = numerosSorteados; return this; }
            public SalaResponseBuilder vencedorUsername(String vencedorUsername) { this.vencedorUsername = vencedorUsername; return this; }
            public SalaResponseBuilder vencedorCartelaId(String vencedorCartelaId) { this.vencedorCartelaId = vencedorCartelaId; return this; }
            public SalaResponse build() {
                return new SalaResponse(id, nome, descricao, precoCartela, maxCartelas, cartelasVendidas, status, numerosSorteados, vencedorUsername, vencedorCartelaId);
            }
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public Double getPrecoCartela() { return precoCartela; }
        public void setPrecoCartela(Double precoCartela) { this.precoCartela = precoCartela; }
        public Integer getMaxCartelas() { return maxCartelas; }
        public void setMaxCartelas(Integer maxCartelas) { this.maxCartelas = maxCartelas; }
        public Integer getCartelasVendidas() { return cartelasVendidas; }
        public void setCartelasVendidas(Integer cartelasVendidas) { this.cartelasVendidas = cartelasVendidas; }
        public StatusSala getStatus() { return status; }
        public void setStatus(StatusSala status) { this.status = status; }
        public List<Integer> getNumerosSorteados() { return numerosSorteados; }
        public void setNumerosSorteados(List<Integer> numerosSorteados) { this.numerosSorteados = numerosSorteados; }
        public String getVencedorUsername() { return vencedorUsername; }
        public void setVencedorUsername(String vencedorUsername) { this.vencedorUsername = vencedorUsername; }
        public String getVencedorCartelaId() { return vencedorCartelaId; }
        public void setVencedorCartelaId(String vencedorCartelaId) { this.vencedorCartelaId = vencedorCartelaId; }
    }

    public static class SalaEventoMessage implements Serializable {
        private String eventoId;
        private String tipoEvento;
        private String salaId;
        private String status;
        private Integer numeroSorteado;
        private String vencedor;
        private LocalDateTime timestamp;

        public SalaEventoMessage() {}

        public SalaEventoMessage(String eventoId, String tipoEvento, String salaId, String status, Integer numeroSorteado, String vencedor, LocalDateTime timestamp) {
            this.eventoId = eventoId;
            this.tipoEvento = tipoEvento;
            this.salaId = salaId;
            this.status = status;
            this.numeroSorteado = numeroSorteado;
            this.vencedor = vencedor;
            this.timestamp = timestamp;
        }

        public static SalaEventoMessageBuilder builder() {
            return new SalaEventoMessageBuilder();
        }

        public static class SalaEventoMessageBuilder {
            private String eventoId;
            private String tipoEvento;
            private String salaId;
            private String status;
            private Integer numeroSorteado;
            private String vencedor;
            private LocalDateTime timestamp;

            public SalaEventoMessageBuilder eventoId(String eventoId) { this.eventoId = eventoId; return this; }
            public SalaEventoMessageBuilder tipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; return this; }
            public SalaEventoMessageBuilder salaId(String salaId) { this.salaId = salaId; return this; }
            public SalaEventoMessageBuilder status(String status) { this.status = status; return this; }
            public SalaEventoMessageBuilder numeroSorteado(Integer numeroSorteado) { this.numeroSorteado = numeroSorteado; return this; }
            public SalaEventoMessageBuilder vencedor(String vencedor) { this.vencedor = vencedor; return this; }
            public SalaEventoMessageBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
            public SalaEventoMessage build() {
                return new SalaEventoMessage(eventoId, tipoEvento, salaId, status, numeroSorteado, vencedor, timestamp);
            }
        }

        public String getEventoId() { return eventoId; }
        public void setEventoId(String eventoId) { this.eventoId = eventoId; }
        public String getTipoEvento() { return tipoEvento; }
        public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
        public String getSalaId() { return salaId; }
        public void setSalaId(String salaId) { this.salaId = salaId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getNumeroSorteado() { return numeroSorteado; }
        public void setNumeroSorteado(Integer numeroSorteado) { this.numeroSorteado = numeroSorteado; }
        public String getVencedor() { return vencedor; }
        public void setVencedor(String vencedor) { this.vencedor = vencedor; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}

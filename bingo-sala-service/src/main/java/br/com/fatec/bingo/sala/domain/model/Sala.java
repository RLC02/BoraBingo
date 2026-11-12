package br.com.fatec.bingo.sala.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sala {
    private String id;
    private String nome;
    private String descricao;
    private Double precoCartela;
    private Integer maxCartelas;
    private Integer cartelasVendidas = 0;
    private StatusSala status = StatusSala.CRIADA;
    private List<Integer> numerosSorteados = new ArrayList<>();
    private String vencedorUsername;
    private String vencedorCartelaId;
    private LocalDateTime criadoEm;
    private Long version;

    public Sala() {
    }

    public Sala(String id, String nome, String descricao, Double precoCartela, Integer maxCartelas,
                Integer cartelasVendidas, StatusSala status, List<Integer> numerosSorteados,
                String vencedorUsername, String vencedorCartelaId, LocalDateTime criadoEm, Long version) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.precoCartela = precoCartela;
        this.maxCartelas = maxCartelas;
        this.cartelasVendidas = cartelasVendidas != null ? cartelasVendidas : 0;
        this.status = status != null ? status : StatusSala.CRIADA;
        this.numerosSorteados = numerosSorteados != null ? numerosSorteados : new ArrayList<>();
        this.vencedorUsername = vencedorUsername;
        this.vencedorCartelaId = vencedorCartelaId;
        this.criadoEm = criadoEm;
        this.version = version;
    }

    public static SalaBuilder builder() {
        return new SalaBuilder();
    }

    public static class SalaBuilder {
        private String id;
        private String nome;
        private String descricao;
        private Double precoCartela;
        private Integer maxCartelas;
        private Integer cartelasVendidas = 0;
        private StatusSala status = StatusSala.CRIADA;
        private List<Integer> numerosSorteados = new ArrayList<>();
        private String vencedorUsername;
        private String vencedorCartelaId;
        private LocalDateTime criadoEm;
        private Long version;

        public SalaBuilder id(String id) { this.id = id; return this; }
        public SalaBuilder nome(String nome) { this.nome = nome; return this; }
        public SalaBuilder descricao(String descricao) { this.descricao = descricao; return this; }
        public SalaBuilder precoCartela(Double precoCartela) { this.precoCartela = precoCartela; return this; }
        public SalaBuilder maxCartelas(Integer maxCartelas) { this.maxCartelas = maxCartelas; return this; }
        public SalaBuilder cartelasVendidas(Integer cartelasVendidas) { this.cartelasVendidas = cartelasVendidas; return this; }
        public SalaBuilder status(StatusSala status) { this.status = status; return this; }
        public SalaBuilder numerosSorteados(List<Integer> numerosSorteados) { this.numerosSorteados = numerosSorteados; return this; }
        public SalaBuilder vencedorUsername(String vencedorUsername) { this.vencedorUsername = vencedorUsername; return this; }
        public SalaBuilder vencedorCartelaId(String vencedorCartelaId) { this.vencedorCartelaId = vencedorCartelaId; return this; }
        public SalaBuilder criadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; return this; }
        public SalaBuilder version(Long version) { this.version = version; return this; }
        public Sala build() {
            return new Sala(id, nome, descricao, precoCartela, maxCartelas, cartelasVendidas, status, numerosSorteados, vencedorUsername, vencedorCartelaId, criadoEm, version);
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
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

package br.com.fatec.bingo.sala.spring.persistence;

import br.com.fatec.bingo.sala.domain.model.StatusSala;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "salas_bingo")
public class SalaDocument {

    @Id
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
    @CreatedDate
    private LocalDateTime criadoEm;

    @Version
    private Long version;


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

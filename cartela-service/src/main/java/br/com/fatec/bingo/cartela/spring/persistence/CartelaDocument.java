package br.com.fatec.bingo.cartela.spring.persistence;

import br.com.fatec.bingo.cartela.domain.model.StatusCompra;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cartelas")
public class CartelaDocument {

    @Id
    private String id;

    private String salaId;
    private String userId;
    private String username;
    private List<List<Integer>> matrizNumeros;
    private List<Integer> marcacoes;
    private StatusCompra status;
    private String motivoRejeicao;
    @CreatedDate
    private LocalDateTime criadoEm;


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

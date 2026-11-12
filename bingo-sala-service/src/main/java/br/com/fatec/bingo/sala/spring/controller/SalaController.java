package br.com.fatec.bingo.sala.spring.controller;

import br.com.fatec.bingo.sala.spring.dto.SalaDTOs.CriarSalaRequest;
import br.com.fatec.bingo.sala.spring.dto.SalaDTOs.SalaResponse;
import br.com.fatec.bingo.sala.spring.service.SalaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    // Rota pública/jogador: Listar todas as salas
    @GetMapping
    public ResponseEntity<List<SalaResponse>> listarSalas() {
        return ResponseEntity.ok(salaService.listarSalas());
    }

    // Rota pública/jogador: Consultar detalhes de uma sala e números sorteados
    @GetMapping("/{id}")
    public ResponseEntity<SalaResponse> obterSala(@PathVariable("id") String id) {
        return ResponseEntity.ok(salaService.obterSala(id));
    }

    // Rota Administrativa: Criar Sala
    @PostMapping("/admin")
    public ResponseEntity<SalaResponse> criarSala(@Valid @RequestBody CriarSalaRequest request) {
        SalaResponse response = salaService.criarSala(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Rota Administrativa: Iniciar Jogo
    @PostMapping("/admin/{id}/iniciar")
    public ResponseEntity<SalaResponse> iniciarJogo(@PathVariable("id") String id) {
        return ResponseEntity.ok(salaService.iniciarJogo(id));
    }

    // Rota Administrativa: Sortear Próximo Número
    @PostMapping("/admin/{id}/sortear")
    public ResponseEntity<SalaResponse> sortearNumero(@PathVariable("id") String id) {
        return ResponseEntity.ok(salaService.sortearProximoNumero(id));
    }

    // Rota Interna/Sistema: Incrementar contagem de cartelas (PATCH legado + POST para RestTemplate)
    @PatchMapping("/{id}/incrementar-cartela")
    public ResponseEntity<Void> incrementarCartelaPatch(@PathVariable("id") String id) {
        salaService.incrementarCartelasVendidas(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/incrementar-cartela")
    public ResponseEntity<Void> incrementarCartelaPost(@PathVariable("id") String id) {
        salaService.incrementarCartelasVendidas(id);
        return ResponseEntity.ok().build();
    }

    // Rota Administrativa: Limpar todas as salas
    @DeleteMapping("/admin/limpar")
    public ResponseEntity<java.util.Map<String, Object>> limparSalas() {
        long total = salaService.limparTodasSalas();
        return ResponseEntity.ok(java.util.Map.of("mensagem", "Salas removidas com sucesso", "total", total));
    }

    // Rota Finalizar Bingo
    @PostMapping("/{id}/bingo")
    public ResponseEntity<SalaResponse> declararBingo(
            @PathVariable("id") String id,
            @RequestParam("username") String username,
            @RequestParam("cartelaId") String cartelaId) {
        return ResponseEntity.ok(salaService.finalizarBingo(id, username, cartelaId));
    }
}

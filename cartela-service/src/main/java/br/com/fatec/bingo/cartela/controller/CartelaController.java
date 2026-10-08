package br.com.fatec.bingo.cartela.controller;

import br.com.fatec.bingo.cartela.dto.CartelaDTOs.*;
import br.com.fatec.bingo.cartela.service.CartelaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cartelas")
public class CartelaController {

    private final CartelaService cartelaService;

    public CartelaController(CartelaService cartelaService) {
        this.cartelaService = cartelaService;
    }

    // Rota para solicitar compra assíncrona de cartela
    @PostMapping("/comprar")
    public ResponseEntity<CartelaResponse> comprarCartela(
            @Valid @RequestBody SolicitarCompraRequest request,
            @RequestHeader(value = "X-Auth-UserId", required = false, defaultValue = "usr-demo") String userId,
            @RequestHeader(value = "X-Auth-Username", required = false, defaultValue = "jogador") String username) {

        CartelaResponse response = cartelaService.solicitarCompraCartela(request.getSalaId(), userId, username);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    // Rota para polling do status da cartela (PENDENTE -> PROCESSANDO -> CONFIRMADO / REJEITADO)
    @GetMapping("/{id}")
    public ResponseEntity<CartelaResponse> obterCartela(@PathVariable("id") String id) {
        return ResponseEntity.ok(cartelaService.obterCartela(id));
    }

    // Rota para listar cartelas do usuário em uma sala
    @GetMapping("/sala/{salaId}")
    public ResponseEntity<List<CartelaResponse>> listarCartelasSala(
            @PathVariable("salaId") String salaId,
            @RequestHeader(value = "X-Auth-Username", required = false, defaultValue = "jogador") String username) {
        return ResponseEntity.ok(cartelaService.listarCartelasUsuario(salaId, username));
    }

    // Rota para marcar número na cartela
    @PostMapping("/{id}/marcar")
    public ResponseEntity<CartelaResponse> marcarNumero(
            @PathVariable("id") String id,
            @RequestBody MarcarNumeroRequest request) {
        return ResponseEntity.ok(cartelaService.marcarNumero(id, request.getNumero()));
    }

    // Rota para bater o Bingo (Validar e Reivindicar Vitória)
    @PostMapping("/{id}/validar-bingo")
    public ResponseEntity<Map<String, Object>> validarBingo(@PathVariable("id") String id) {
        boolean bingoValido = cartelaService.validarBingo(id);
        if (bingoValido) {
            return ResponseEntity.ok(Map.of("resultado", "BINGO_VALIDO", "mensagem", "Parabéns! Você venceu o Bingo!"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("resultado", "BINGO_INVALIDO", "mensagem", "Sua cartela ainda não completou todos os números sorteados."));
        }
    }
}

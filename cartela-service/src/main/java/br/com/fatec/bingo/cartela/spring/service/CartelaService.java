package br.com.fatec.bingo.cartela.spring.service;

import br.com.fatec.bingo.cartela.spring.dto.CartelaDTOs.*;
import br.com.fatec.bingo.cartela.domain.model.Cartela;
import br.com.fatec.bingo.cartela.domain.model.StatusCompra;
import br.com.fatec.bingo.cartela.domain.repository.CartelaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CartelaService {

    private static final Logger log = LoggerFactory.getLogger(CartelaService.class);

    private final CartelaRepository cartelaRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;

    @Value("${bingo.rabbitmq.exchange}")
    private String exchange;

    @Value("${bingo.rabbitmq.routingkey.compra-solicitada}")
    private String routingKeySolicitada;

    @Value("${services.sala-url}")
    private String salaServiceUrl;

    @Value("${services.auth-url}")
    private String authServiceUrl;

    public CartelaService(CartelaRepository cartelaRepository, RabbitTemplate rabbitTemplate, RestTemplate restTemplate) {
        this.cartelaRepository = cartelaRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.restTemplate = restTemplate;
    }

    /**
     * Passo 1: Recebe a intenção de compra, persiste a Cartela com status PENDENTE (nunca nulo)
     * e publica mensagem na fila do RabbitMQ para processamento assíncrono.
     */
    @Transactional
    public CartelaResponse solicitarCompraCartela(String salaId, String userId, String username) {
        Cartela cartela = Cartela.builder()
                .salaId(salaId)
                .userId(userId)
                .username(username)
                .status(StatusCompra.PENDENTE) // Inicialização obrigatória de status
                .matrizNumeros(new ArrayList<>())
                .marcacoes(new ArrayList<>())
                .criadoEm(LocalDateTime.now())
                .build();

        cartela = cartelaRepository.save(cartela);

        // Envia mensagem para fila assíncrona
        CompraMessage msg = CompraMessage.builder()
                .cartelaId(cartela.getId())
                .salaId(salaId)
                .userId(userId)
                .username(username)
                .status(cartela.getStatus())
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(exchange, routingKeySolicitada, msg);
        log.info("Compra de cartela solicitada (status PENDENTE): cartelaId={}, user={}", cartela.getId(), username);

        return toResponse(cartela);
    }

    /**
     * Passo 2: Executado pelo Consumer da fila RabbitMQ sob transação MongoDB (@Transactional).
     * Garante concorrência e integridade das regras de negócio.
     */
    @Transactional
    public void processarCompraFila(CompraMessage msg) {
        log.info("Consumer iniciando processamento da compra: cartelaId={}", msg.getCartelaId());

        Optional<Cartela> optCartela = cartelaRepository.findById(msg.getCartelaId());
        if (optCartela.isEmpty()) {
            log.error("Cartela não encontrada para processar compra: {}", msg.getCartelaId());
            return;
        }

        Cartela cartela = optCartela.get();
        cartela.setStatus(StatusCompra.PROCESSANDO);
        cartela = cartelaRepository.save(cartela);

        try {
            // Consulta dados atualizados da Sala via API
            SalaInfoDTO sala = restTemplate.getForObject(salaServiceUrl + "/" + cartela.getSalaId(), SalaInfoDTO.class);
            if (sala == null) {
                rejeitarCartela(cartela, "Sala inexistente");
                return;
            }

            if (!"CRIADA".equalsIgnoreCase(sala.getStatus())) {
                rejeitarCartela(cartela, "A sala já iniciou ou foi encerrada");
                return;
            }

            // Validação de vagas / concorrência de cartelas (conta apenas CONFIRMADO para evitar false-positives com PENDENTE/PROCESSANDO)
            long cartelasConfirmadas = cartelaRepository.countBySalaIdAndStatus(cartela.getSalaId(), StatusCompra.CONFIRMADO);
            if (cartelasConfirmadas >= sala.getMaxCartelas()) {
                rejeitarCartela(cartela, "Limite máximo de cartelas atingido para esta sala");
                return;
            }

            // Debita saldo do usuário no auth-service (passa X-Auth-Username para o endpoint interno)
            try {
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("X-Auth-Username", cartela.getUsername());
                org.springframework.http.HttpEntity<Void> req = new org.springframework.http.HttpEntity<>(headers);
                restTemplate.postForObject(
                    authServiceUrl + "/debitar?valor=" + sala.getPrecoCartela(),
                    req,
                    Void.class
                );
            } catch (Exception ex) {
                // Se o auth-service retornar erro (ex: saldo insuficiente), rejeita a cartela
                rejeitarCartela(cartela, "Saldo insuficiente para comprar a cartela.");
                return;
            }

            // Incrementa contagem na Sala
            restTemplate.postForObject(
                salaServiceUrl + "/" + cartela.getSalaId() + "/incrementar-cartela",
                null,
                Void.class
            );

            // Gera a matriz 5x5 do Bingo clássico (1-75)
            List<List<Integer>> matriz = gerarMatrizBingo();
            cartela.setMatrizNumeros(matriz);
            cartela.setStatus(StatusCompra.CONFIRMADO);
            cartela.setMarcacoes(new ArrayList<>());
            cartelaRepository.save(cartela);

            log.info("Compra de cartela CONFIRMADA com sucesso! cartelaId={}", cartela.getId());

        } catch (Exception e) {
            log.error("Erro ao processar compra de cartela: {}", e.getMessage(), e);
            rejeitarCartela(cartela, "Erro interno de concorrência: " + e.getMessage());
        }
    }

    private void rejeitarCartela(Cartela cartela, String motivo) {
        cartela.setStatus(StatusCompra.REJEITADO);
        cartela.setMotivoRejeicao(motivo);
        cartelaRepository.save(cartela);
        log.warn("Compra de cartela REJEITADA: cartelaId={}, motivo={}", cartela.getId(), motivo);
    }

    public List<CartelaResponse> listarCartelasUsuario(String salaId, String username) {
        return cartelaRepository.findBySalaIdAndUsername(salaId, username).stream()
                .map(this::toResponse)
                .toList();
    }

    public CartelaResponse obterCartela(String id) {
        Cartela cartela = cartelaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cartela não encontrada"));
        return toResponse(cartela);
    }

    @Transactional
    public CartelaResponse marcarNumero(String cartelaId, Integer numero) {
        Cartela cartela = cartelaRepository.findById(cartelaId)
                .orElseThrow(() -> new IllegalArgumentException("Cartela não encontrada"));

        if (cartela.getStatus() != StatusCompra.CONFIRMADO) {
            throw new IllegalStateException("Cartela não confirmada para jogo");
        }

        if (cartela.getMarcacoes() == null) {
            cartela.setMarcacoes(new ArrayList<>());
        }

        if (!cartela.getMarcacoes().contains(numero)) {
            cartela.getMarcacoes().add(numero);
            cartela = cartelaRepository.save(cartela);
        }

        return toResponse(cartela);
    }

    @Transactional
    public boolean validarBingo(String cartelaId) {
        Cartela cartela = cartelaRepository.findById(cartelaId)
                .orElseThrow(() -> new IllegalArgumentException("Cartela não encontrada"));

        // Cartela deve estar CONFIRMADA com matriz gerada
        if (cartela.getStatus() != StatusCompra.CONFIRMADO
                || cartela.getMatrizNumeros() == null
                || cartela.getMatrizNumeros().isEmpty()) {
            return false;
        }

        // Busca números sorteados na Sala
        SalaInfoDTO sala = restTemplate.getForObject(salaServiceUrl + "/" + cartela.getSalaId(), SalaInfoDTO.class);
        if (sala == null || sala.getNumerosSorteados() == null) {
            return false;
        }

        List<Integer> sorteados = sala.getNumerosSorteados();

        // Checa se todos os números da cartela (exceto o centro FREE = 0) foram sorteados
        for (List<Integer> coluna : cartela.getMatrizNumeros()) {
            for (Integer num : coluna) {
                if (num != 0 && !sorteados.contains(num)) {
                    return false; // Ainda falta número ser sorteado
                }
            }
        }

        // Informa à sala que temos um vencedor
        restTemplate.postForObject(
                salaServiceUrl + "/" + cartela.getSalaId() + "/bingo?username=" + cartela.getUsername() + "&cartelaId=" + cartelaId,
                null,
                Void.class
        );

        // Credita o prêmio: total arrecadado = cartelasVendidas × precoCartela
        try {
            double premio = (sala.getCartelasVendidas() != null ? sala.getCartelasVendidas() : 1)
                          * (sala.getPrecoCartela()     != null ? sala.getPrecoCartela()     : 0.0);
            if (premio > 0) {
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("X-Auth-Username", cartela.getUsername());
                org.springframework.http.HttpEntity<Void> req = new org.springframework.http.HttpEntity<>(headers);
                restTemplate.postForObject(
                        authServiceUrl + "/creditar?valor=" + premio,
                        req,
                        Void.class
                );
                log.info("Prêmio de R$ {} creditado para o vencedor: {}", premio, cartela.getUsername());
            }
        } catch (Exception ex) {
            log.error("Erro ao creditar prêmio para {}: {}", cartela.getUsername(), ex.getMessage());
        }

        return true;
    }

    private List<List<Integer>> gerarMatrizBingo() {
        // Coluna B (1-15), I (16-30), N (31-45), G (46-60), O (61-75)
        List<List<Integer>> matriz = new ArrayList<>();
        Random rand = new Random();

        int[][] faixas = {{1, 15}, {16, 30}, {31, 45}, {46, 60}, {61, 75}};

        for (int col = 0; col < 5; col++) {
            Set<Integer> colunaNumeros = new HashSet<>();
            int min = faixas[col][0];
            int max = faixas[col][1];

            while (colunaNumeros.size() < 5) {
                colunaNumeros.add(rand.nextInt(max - min + 1) + min);
            }

            List<Integer> colList = new ArrayList<>(colunaNumeros);
            Collections.sort(colList);

            if (col == 2) {
                // Posição central é FREE (0)
                colList.set(2, 0);
            }

            matriz.add(colList);
        }

        return matriz;
    }

    private CartelaResponse toResponse(Cartela cartela) {
        return CartelaResponse.builder()
                .id(cartela.getId())
                .salaId(cartela.getSalaId())
                .userId(cartela.getUserId())
                .username(cartela.getUsername())
                .matrizNumeros(cartela.getMatrizNumeros())
                .marcacoes(cartela.getMarcacoes())
                .status(cartela.getStatus())
                .motivoRejeicao(cartela.getMotivoRejeicao())
                .build();
    }
}

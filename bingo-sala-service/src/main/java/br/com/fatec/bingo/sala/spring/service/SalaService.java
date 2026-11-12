package br.com.fatec.bingo.sala.spring.service;

import br.com.fatec.bingo.sala.spring.dto.SalaDTOs.CriarSalaRequest;
import br.com.fatec.bingo.sala.spring.dto.SalaDTOs.SalaEventoMessage;
import br.com.fatec.bingo.sala.spring.dto.SalaDTOs.SalaResponse;
import br.com.fatec.bingo.sala.domain.model.Sala;
import br.com.fatec.bingo.sala.domain.model.StatusSala;
import br.com.fatec.bingo.sala.domain.repository.SalaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SalaService {

    private static final Logger log = LoggerFactory.getLogger(SalaService.class);

    private final SalaRepository salaRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${bingo.rabbitmq.exchange}")
    private String exchange;

    public SalaService(SalaRepository salaRepository, RabbitTemplate rabbitTemplate) {
        this.salaRepository = salaRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public SalaResponse criarSala(CriarSalaRequest request) {
        Sala sala = Sala.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .precoCartela(request.getPrecoCartela())
                .maxCartelas(request.getMaxCartelas())
                .cartelasVendidas(0)
                .status(StatusSala.CRIADA)
                .numerosSorteados(new ArrayList<>())
                .criadoEm(LocalDateTime.now())
                .build();

        sala = salaRepository.save(sala);

        // Publica Evento no RabbitMQ
        SalaEventoMessage evento = SalaEventoMessage.builder()
                .eventoId(UUID.randomUUID().toString())
                .tipoEvento("SALA_CRIADA")
                .salaId(sala.getId())
                .status(sala.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(exchange, "bingo.sala.criada", evento);
        log.info("Sala criada e evento publicado: id={}", sala.getId());

        return toResponse(sala);
    }

    public List<SalaResponse> listarSalas() {
        return salaRepository.findAll().stream().map(this::toResponse).toList();
    }

    public SalaResponse obterSala(String salaId) {
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada: " + salaId));
        return toResponse(sala);
    }

    @Transactional
    public SalaResponse iniciarJogo(String salaId) {
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada: " + salaId));

        if (sala.getStatus() != StatusSala.CRIADA) {
            throw new IllegalStateException("A sala só pode ser iniciada se estiver com status CRIADA");
        }

        sala.setStatus(StatusSala.EM_ANDAMENTO);
        sala = salaRepository.save(sala);

        SalaEventoMessage evento = SalaEventoMessage.builder()
                .eventoId(UUID.randomUUID().toString())
                .tipoEvento("SALA_INICIADA")
                .salaId(sala.getId())
                .status(sala.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(exchange, "bingo.sala.iniciada", evento);
        log.info("Sala iniciada: id={}", sala.getId());

        return toResponse(sala);
    }

    @Transactional
    public SalaResponse sortearProximoNumero(String salaId) {
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada"));

        if (sala.getStatus() != StatusSala.EM_ANDAMENTO) {
            throw new IllegalStateException("O jogo não está em andamento");
        }

        if (sala.getNumerosSorteados().size() >= 75) {
            throw new IllegalStateException("Todos os números de 1 a 75 já foram sorteados");
        }

        List<Integer> disponiveis = new ArrayList<>();
        for (int i = 1; i <= 75; i++) {
            if (!sala.getNumerosSorteados().contains(i)) {
                disponiveis.add(i);
            }
        }

        Collections.shuffle(disponiveis);
        Integer sorteado = disponiveis.get(0);

        sala.getNumerosSorteados().add(sorteado);
        sala = salaRepository.save(sala);

        SalaEventoMessage evento = SalaEventoMessage.builder()
                .eventoId(UUID.randomUUID().toString())
                .tipoEvento("NUMERO_SORTEADO")
                .salaId(sala.getId())
                .numeroSorteado(sorteado)
                .status(sala.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(exchange, "bingo.numero.sorteado", evento);
        log.info("Número sorteado na sala {}: {}", sala.getId(), sorteado);

        return toResponse(sala);
    }

    @Transactional
    public SalaResponse finalizarBingo(String salaId, String vencedorUsername, String cartelaId) {
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada"));

        sala.setStatus(StatusSala.FINALIZADA);
        sala.setVencedorUsername(vencedorUsername);
        sala.setVencedorCartelaId(cartelaId);
        sala = salaRepository.save(sala);

        SalaEventoMessage evento = SalaEventoMessage.builder()
                .eventoId(UUID.randomUUID().toString())
                .tipoEvento("BINGO_FINALIZADO")
                .salaId(sala.getId())
                .vencedor(vencedorUsername)
                .status(sala.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(exchange, "bingo.sala.finalizada", evento);
        log.info("Bingo finalizado na sala {}. Vencedor: {}", sala.getId(), vencedorUsername);

        return toResponse(sala);
    }

    public long limparTodasSalas() {
        long total = salaRepository.count();
        salaRepository.deleteAll();
        log.info("Admin limpou todas as salas. Total removido: {}", total);
        return total;
    }

    @Transactional
    public void incrementarCartelasVendidas(String salaId) {
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada"));

        if (sala.getCartelasVendidas() >= sala.getMaxCartelas()) {
            throw new IllegalStateException("Capacidade máxima de cartelas esgotada para esta sala");
        }

        sala.setCartelasVendidas(sala.getCartelasVendidas() + 1);
        try {
            salaRepository.save(sala);
        } catch (OptimisticLockingFailureException e) {
            // Conflito de concorrência detectado pelo @Version — outra thread modificou a sala
            log.warn("Conflito de concorrência ao incrementar cartelas na sala {}: {}", salaId, e.getMessage());
            throw new IllegalStateException("Capacidade máxima de cartelas esgotada para esta sala");
        }
    }

    private SalaResponse toResponse(Sala sala) {
        return SalaResponse.builder()
                .id(sala.getId())
                .nome(sala.getNome())
                .descricao(sala.getDescricao())
                .precoCartela(sala.getPrecoCartela())
                .maxCartelas(sala.getMaxCartelas())
                .cartelasVendidas(sala.getCartelasVendidas())
                .status(sala.getStatus())
                .numerosSorteados(sala.getNumerosSorteados())
                .vencedorUsername(sala.getVencedorUsername())
                .vencedorCartelaId(sala.getVencedorCartelaId())
                .build();
    }
}

package br.com.fatec.bingo.cartela.consumer;

import br.com.fatec.bingo.cartela.dto.CartelaDTOs.CompraMessage;
import br.com.fatec.bingo.cartela.service.CartelaService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CompraCartelaConsumer {

    private static final Logger log = LoggerFactory.getLogger(CompraCartelaConsumer.class);

    private final CartelaService cartelaService;

    public CompraCartelaConsumer(CartelaService cartelaService) {
        this.cartelaService = cartelaService;
    }

    @RabbitListener(queues = "${bingo.rabbitmq.queue.compra-cartela}", ackMode = "MANUAL")
    public void processarCompraCartela(
            @Payload CompraMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        log.info("Mensagem recebida na fila de compras: cartelaId={}", message.getCartelaId());

        try {
            // Processa a validação da compra com garantia transacional
            cartelaService.processarCompraFila(message);

            // Confirmação manual de processamento com sucesso (ACK)
            channel.basicAck(tag, false);
            log.info("ACK enviado com sucesso para a mensagem tag={}", tag);

        } catch (Exception e) {
            log.error("Falha irrecuperável no processamento da mensagem tag={}. Enviando NACK para DLQ...", tag, e);
            // Rejeição da mensagem (NACK com requeue=false para rotear à Dead Letter Queue)
            channel.basicNack(tag, false, false);
        }
    }
}

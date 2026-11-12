package br.com.fatec.bingo.cartela.spring.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CartelaRabbitAndMongoConfig {

    @Value("${bingo.rabbitmq.exchange}")
    private String exchange;

    @Value("${bingo.rabbitmq.queue.compra-cartela}")
    private String compraQueue;

    @Value("${bingo.rabbitmq.queue.compra-dlq}")
    private String compraDlq;

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public TopicExchange bingoExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue compraDlqQueue() {
        return QueueBuilder.durable(compraDlq).build();
    }

    @Bean
    public Queue compraCartelaQueue() {
        return QueueBuilder.durable(compraQueue)
                .withArgument("x-dead-letter-exchange", exchange)
                .withArgument("x-dead-letter-routing-key", "bingo.cartela.dlq")
                .build();
    }

    @Bean
    public Binding bindingCompraCartela(Queue compraCartelaQueue, TopicExchange bingoExchange) {
        return BindingBuilder.bind(compraCartelaQueue).to(bingoExchange).with("bingo.cartela.compra.solicitada");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}

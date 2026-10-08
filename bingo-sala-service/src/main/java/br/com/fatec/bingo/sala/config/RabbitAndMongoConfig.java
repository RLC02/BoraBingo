package br.com.fatec.bingo.sala.config;

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

@Configuration
public class RabbitAndMongoConfig {

    @Value("${bingo.rabbitmq.exchange}")
    private String exchange;

    @Value("${bingo.rabbitmq.queue.sala-eventos}")
    private String salaQueue;

    @Value("${bingo.rabbitmq.queue.sorteio-eventos}")
    private String sorteioQueue;

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Bean
    public TopicExchange bingoExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue salaEventosQueue() {
        return QueueBuilder.durable(salaQueue).build();
    }

    @Bean
    public Queue sorteioEventosQueue() {
        return QueueBuilder.durable(sorteioQueue).build();
    }

    @Bean
    public Binding bindingSalaCriada(Queue salaEventosQueue, TopicExchange bingoExchange) {
        return BindingBuilder.bind(salaEventosQueue).to(bingoExchange).with("bingo.sala.*");
    }

    @Bean
    public Binding bindingSorteio(Queue sorteioEventosQueue, TopicExchange bingoExchange) {
        return BindingBuilder.bind(sorteioEventosQueue).to(bingoExchange).with("bingo.numero.*");
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

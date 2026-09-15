package br.com.marlon.voting_api.messaging.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
        @Bean
        public Queue votingResultsQueue(
                @Value("${app.messaging.voting-result.queue}") String queueName
        ) {
            return QueueBuilder.durable(queueName).build();
        }

        @Bean
        public MessageConverter messageConverter() {
            return new JacksonJsonMessageConverter();
        }


}

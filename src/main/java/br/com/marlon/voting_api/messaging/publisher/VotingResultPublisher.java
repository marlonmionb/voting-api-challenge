package br.com.marlon.voting_api.messaging.publisher;

import br.com.marlon.voting_api.messaging.event.VotingResultEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VotingResultPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String queueName;

    public VotingResultPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.messaging.voting-result.queue}") String queueName
    )  {
        this.rabbitTemplate = rabbitTemplate;
        this.queueName = queueName;
    }

    public void publish(VotingResultEvent event) {
        rabbitTemplate.convertAndSend(queueName, event);
    }


}

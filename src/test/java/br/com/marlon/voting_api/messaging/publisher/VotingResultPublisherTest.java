package br.com.marlon.voting_api.messaging.publisher;

import br.com.marlon.voting_api.messaging.event.VotingResultEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VotingResultPublisherTest {

    @Test
    void shouldSendVotingResultEventToConfiguredQueue() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        VotingResultPublisher publisher = new VotingResultPublisher(
                rabbitTemplate,
                "voting-results"
        );
        VotingResultEvent event = new VotingResultEvent(
                1L,
                2L,
                "Approve annual budget",
                3,
                1,
                4,
                "APPROVED",
                OffsetDateTime.now().minusMinutes(1)
        );

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend("voting-results", event);
    }
}

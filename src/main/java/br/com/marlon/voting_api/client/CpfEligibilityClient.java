package br.com.marlon.voting_api.client;

import br.com.marlon.voting_api.exception.ExternalServiceUnavailableException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

@Component
public class CpfEligibilityClient {
    private final RestClient restClient;

    public CpfEligibilityClient(
            @Value("${external.cpf-eligibility.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public VoteEligibility checkEligibility(String cpf) {
        try {
            CpfEligibilityResponse response = restClient.get()
                    .uri("users/{cpf}", cpf)
                    .retrieve()
                    .body(CpfEligibilityResponse.class);

            if (response == null) {
                throw new ExternalServiceUnavailableException(
                        "CPF eligibility service returned an empty response",
                        null
                );
            }

            return VoteEligibility.valueOf(response.status());
        } catch (RestClientResponseException exception){
            if (exception.getStatusCode().value() == 404) {
                throw new EntityNotFoundException(
                        "CPF was not found by the eligibility service"
                );
            }

            throw new ExternalServiceUnavailableException(
                    "CPF eligibility service is unavailable",
                    exception
            );
        } catch (ResourceAccessException exception) {
            throw new ExternalServiceUnavailableException(
                    "CPF eligibility service is unavailable",
                    exception
            );
        }

    }

    private record CpfEligibilityResponse(String status) {

    }

}

package br.com.marlon.voting_api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
		info = @Info(
				title = "Voting API",
				version = "v1",
				description = "API for agenda items, voting sessions, and votes."
		)
)
public class VotingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(VotingApiApplication.class, args);
	}

}

package br.com.marlon.voting_api;

import org.springframework.boot.SpringApplication;

public class TestVotingApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(VotingApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

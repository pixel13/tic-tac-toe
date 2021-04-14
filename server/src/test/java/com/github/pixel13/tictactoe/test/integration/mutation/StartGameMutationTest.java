package com.github.pixel13.tictactoe.test.integration.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.test.integration.IntegrationTest;
import com.graphql.spring.boot.test.GraphQLResponse;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("The 'startGame' mutation")
class StartGameMutationTest extends IntegrationTest {

  @Autowired
  private Flux<Game> gamePublisher;

  @Test
  @DisplayName("registers a player as the first player of a game")
  void firstPlayer() throws IOException {
    ObjectNode variables = variable("playerName", FIRST_PLAYER_NAME);
    GraphQLResponse response = graphQLTestTemplate.perform("graphql/mutation/startGame.graphql", variables);

    assertTrue(response.isOk());
    assertEquals(FIRST_PLAYER_NAME, response.get("$.data.startGame.name"));
  }

  @Test
  @DisplayName("registers a player as the second player of a game and sends data through the reactive stream")
  void secondPlayer() throws IOException {
    registerPlayer(FIRST_PLAYER_NAME);
    ObjectNode variables = variable("playerName", SECOND_PLAYER_NAME);
    GraphQLResponse response = graphQLTestTemplate.perform("graphql/mutation/startGame.graphql", variables);

    assertTrue(response.isOk());
    assertEquals(SECOND_PLAYER_NAME, response.get("$.data.startGame.name"));
    StepVerifier.create(gamePublisher)
        .expectNextMatches(game -> !game.isWaitingForSecondPlayer() && game.getSecondPlayer().getName().equals(SECOND_PLAYER_NAME))
        .thenCancel()
        .verify(Duration.ofSeconds(MAX_SUBSCRIPTION_TIMEOUT));
  }

}

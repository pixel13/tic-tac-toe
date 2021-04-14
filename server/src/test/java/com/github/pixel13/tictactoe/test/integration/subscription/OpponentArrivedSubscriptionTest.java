package com.github.pixel13.tictactoe.test.integration.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.pixel13.tictactoe.test.integration.IntegrationTest;
import com.graphql.spring.boot.test.GraphQLResponse;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

@DisplayName("The 'opponentArrived' subscription")
class OpponentArrivedSubscriptionTest extends IntegrationTest {

  private static final int MAX_WAIT_TIME = 3000;

  @Test
  @DisplayName("returns an error if user is not authenticated")
  void notAuthenticated() {
    GraphQLResponse result = graphQLTestSubscription
        .init()
        .start("graphql/subscription/opponentArrived.graphql")
        .awaitAndGetNextResponse(MAX_WAIT_TIME);

    assertTrue(result.isOk());
    assertUnauthorized(result);
  }

  @Test
  @DisplayName("sends the opponent's name as soon as he/she arrives")
  void sendOpponentName() throws IOException {
    AuthenticatedGraphQLTestTemplate firstPlayer = registerPlayer(FIRST_PLAYER_NAME);

    graphQLTestSubscription
        .init(Map.of(HttpHeaders.AUTHORIZATION, "Bearer " + firstPlayer.getToken()))
        .start("graphql/subscription/opponentArrived.graphql");

    graphQLTestTemplate.perform("graphql/mutation/startGame.graphql", variable("playerName", SECOND_PLAYER_NAME));

    GraphQLResponse result = graphQLTestSubscription.awaitAndGetNextResponse(MAX_WAIT_TIME);

    assertTrue(result.isOk());
    assertEquals(SECOND_PLAYER_NAME, result.get("$.data.opponentArrived"));
  }

}

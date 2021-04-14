package com.github.pixel13.tictactoe.test.integration.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.pixel13.tictactoe.test.integration.IntegrationTest;
import com.graphql.spring.boot.test.GraphQLResponse;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The 'gameStatus' query")
class GameStatusQueryTest extends IntegrationTest {

  @Test
  @DisplayName("returns an error if user is not authenticated")
  void notAuthenticated() throws IOException {
    GraphQLResponse response = graphQLTestTemplate.postForResource("graphql/query/gameStatus.graphql");

    assertTrue(response.isOk());
    assertUnauthorized(response);
  }

  @Test
  @DisplayName("returns current game status fon an authenticated user")
  void authenticated() throws IOException {
    AuthenticatedGraphQLTestTemplate firstPlayerTemplate = registerPlayer(FIRST_PLAYER_NAME);
    GraphQLResponse response = firstPlayerTemplate.postForResource("graphql/query/gameStatus.graphql");

    assertTrue(response.isOk());
    assertFalse(response.get("$.data.gameStatus.isStarted", Boolean.class));
    assertFalse(response.get("$.data.gameStatus.isOver", Boolean.class));
    assertNull(response.get("$.data.gameStatus.winner"));
    assertEquals(FIRST_PLAYER_NAME, response.get("$.data.gameStatus.onMove"));
  }

}

package com.github.pixel13.tictactoe.test.integration.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.pixel13.tictactoe.domain.Cell;
import com.github.pixel13.tictactoe.test.integration.IntegrationTest;
import com.graphql.spring.boot.test.GraphQLResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

@DisplayName("The 'opponentMove' subscription")
class OpponentMoveSubscriptionTest extends IntegrationTest {

  private static final int MAX_WAIT_TIME = 3000;

  @Test
  @DisplayName("returns an error if user is not authenticated")
  void notAuthenticated() {
    GraphQLResponse result = graphQLTestSubscription
        .init()
        .start("graphql/subscription/opponentMove.graphql")
        .awaitAndGetNextResponse(MAX_WAIT_TIME);

    assertTrue(result.isOk());
    assertUnauthorized(result);
  }

  @Test
  @DisplayName("sends the opponent's move as soon as he/she makes his/her own move")
  void sendOpponentMove() throws IOException {
    AuthenticatedGraphQLTestTemplate firstPlayer = registerPlayer(FIRST_PLAYER_NAME);
    AuthenticatedGraphQLTestTemplate secondPlayer = registerPlayer(SECOND_PLAYER_NAME);

    // First move
    graphQLTestSubscription
        .init(Map.of(HttpHeaders.AUTHORIZATION, "Bearer " + secondPlayer.getToken()))
        .start("graphql/subscription/opponentMove.graphql");

    move(firstPlayer, 1, 1);

    GraphQLResponse result1 = graphQLTestSubscription.awaitAndGetNextResponse(MAX_WAIT_TIME);

    assertTrue(result1.isOk());
    assertEquals(SECOND_PLAYER_NAME, result1.get("$.data.opponentMove.onMove"));
    List<Cell> cells1 = result1.getList("$.data.opponentMove.board", Cell.class);
    assertTrue(cells1.stream().filter(c -> c.getRow() == 1 && c.getColumn() == 1).map(Cell::getValue).noneMatch(String::isEmpty));

    graphQLTestSubscription.reset();

    // Second move
    graphQLTestSubscription
        .init(Map.of(HttpHeaders.AUTHORIZATION, "Bearer " + firstPlayer.getToken()))
        .start("graphql/subscription/opponentMove.graphql");

    move(secondPlayer, 2, 2);

    GraphQLResponse result2 = graphQLTestSubscription.awaitAndGetNextResponse(MAX_WAIT_TIME);

    assertTrue(result2.isOk());
    assertEquals(FIRST_PLAYER_NAME, result2.get("$.data.opponentMove.onMove"));
    List<Cell> cells2 = result2.getList("$.data.opponentMove.board", Cell.class);
    assertTrue(cells2.stream().filter(c -> c.getRow() == 2 && c.getColumn() == 2).map(Cell::getValue).noneMatch(String::isEmpty));
  }

}

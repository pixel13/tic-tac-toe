package com.github.pixel13.tictactoe.test.integration.mutation;

import static com.github.pixel13.tictactoe.exception.GraphQLExecutionException.ERROR_CODE_ILLEGAL_MOVE;
import static com.github.pixel13.tictactoe.exception.GraphQLExecutionException.ERROR_CODE_INVALID_COORDINATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.pixel13.tictactoe.domain.Cell;
import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.test.integration.IntegrationTest;
import com.graphql.spring.boot.test.GraphQLResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("The 'move' mutation")
class MoveMutationTest extends IntegrationTest {

  private static final int ROW = 1;
  private static final int COLUMN = 2;

  @Autowired
  private Flux<Game> gamePublisher;

  @Test
  @DisplayName("returns an error if trying to make a move without authentication")
  void notAuthenticated() throws IOException {
    GraphQLResponse response = move(graphQLTestTemplate, ROW, COLUMN);

    assertTrue(response.isOk());
    assertUnauthorized(response);
  }

  @Test
  @DisplayName("returns an error if a move is made when the game is not yet started")
  void notYetStarted() throws IOException {
    AuthenticatedGraphQLTestTemplate firstPlayerTemplate = registerPlayer(FIRST_PLAYER_NAME);
    GraphQLResponse response = move(firstPlayerTemplate, ROW, COLUMN);

    assertTrue(response.isOk());
    assertGraphQLError(response, ERROR_CODE_ILLEGAL_MOVE);
  }

  @Test
  @DisplayName("returns an error if the player that's making the move is not in turn")
  void notInTurn() throws IOException {
    registerPlayer(FIRST_PLAYER_NAME);
    AuthenticatedGraphQLTestTemplate secondPlayerTemplate = registerPlayer(SECOND_PLAYER_NAME);
    GraphQLResponse response = move(secondPlayerTemplate, ROW, COLUMN);

    assertTrue(response.isOk());
    assertGraphQLError(response, ERROR_CODE_ILLEGAL_MOVE);
  }

  @Test
  @DisplayName("returns an error if an invalid coordinate is given")
  void invalidCoordinate() throws IOException {
    AuthenticatedGraphQLTestTemplate firstPlayerTemplate = registerPlayer(FIRST_PLAYER_NAME);
    registerPlayer(SECOND_PLAYER_NAME);
    GraphQLResponse response = move(firstPlayerTemplate, 10, COLUMN);

    assertTrue(response.isOk());
    assertGraphQLError(response, ERROR_CODE_INVALID_COORDINATE);
  }

  @Test
  @DisplayName("updates the board, changes the turn and sends data though the reactive stream if move is valid")
  void validMove() throws IOException {
    AuthenticatedGraphQLTestTemplate firstPlayerTemplate = registerPlayer(FIRST_PLAYER_NAME);
    registerPlayer(SECOND_PLAYER_NAME);
    GraphQLResponse response = move(firstPlayerTemplate, ROW, COLUMN);

    assertTrue(response.isOk());
    assertEquals(SECOND_PLAYER_NAME, response.get("$.data.move.onMove"));
    List<Cell> cells = response.getList("$.data.move.board", Cell.class);
    assertTrue(cells.stream().filter(c -> c.getRow() == ROW && c.getColumn() == COLUMN).map(Cell::getValue).noneMatch(String::isEmpty));

    StepVerifier.create(gamePublisher)
        .expectNextMatches(
            g -> g.getFirstPlayer().getName().equals(FIRST_PLAYER_NAME) && g.getSecondPlayer().getName().equals(SECOND_PLAYER_NAME))
        .thenCancel()
        .verify(Duration.ofSeconds(MAX_SUBSCRIPTION_TIMEOUT));
  }

  @Test
  @DisplayName("allows two player to play a full match, then forbid any subsequent move when the game is over")
  void fullPlay() throws IOException {
    AuthenticatedGraphQLTestTemplate firstPlayer = registerPlayer(FIRST_PLAYER_NAME);
    AuthenticatedGraphQLTestTemplate secondPlayer = registerPlayer(SECOND_PLAYER_NAME);
    move(firstPlayer, 0, 0);
    move(secondPlayer, 1, 0);
    move(firstPlayer, 1, 1);
    move(secondPlayer, 0, 1);
    GraphQLResponse response = move(firstPlayer, 2, 2);

    assertTrue(response.isOk());
    assertEquals(SECOND_PLAYER_NAME, response.get("$.data.move.onMove"));
    assertTrue(response.get("$.data.move.isOver", Boolean.class));
    assertEquals(FIRST_PLAYER_NAME, response.get("$.data.move.winner"));

    GraphQLResponse moveAfterGameIsOver = move(secondPlayer, 2, 0);
    assertTrue(moveAfterGameIsOver.isOk());
    assertGraphQLError(moveAfterGameIsOver, ERROR_CODE_ILLEGAL_MOVE);
  }

}

package com.github.pixel13.tictactoe.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.domain.Player;
import com.github.pixel13.tictactoe.exception.IllegalMoveException;
import com.github.pixel13.tictactoe.exception.InvalidCoordinateException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("GameServiceImpl")
class GameServiceImplTest {

  private static final String FIRST_PLAYER_NAME = "firstPlayer";
  private static final String SECOND_PLAYER_NAME = "secondPlayer";
  private static final String THIRD_PLAYER_NAME = "thirdPlayer";
  private static final int ROW = 1;
  private static final int COLUMN = 1;

  private GameServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new GameServiceImpl();
  }

  @Test
  @DisplayName("creates a new player and assign it as first player of a new game")
  void registerPlayerFirstPlayer() {
    Game result = service.registerPlayer(FIRST_PLAYER_NAME);
    assertEquals(FIRST_PLAYER_NAME, result.getFirstPlayer().getName());
    assertTrue(result.isWaitingForSecondPlayer());
  }

  @Test
  @DisplayName("creates a new player and assign it as second player if exists a game that's waiting for a player")
  void registerPlayerSecondPlayer() {
    Game previous = service.registerPlayer(FIRST_PLAYER_NAME);
    Game result = service.registerPlayer(SECOND_PLAYER_NAME);

    assertEquals(previous, result);
    assertEquals(FIRST_PLAYER_NAME, result.getFirstPlayer().getName());
    assertEquals(SECOND_PLAYER_NAME, result.getSecondPlayer().getName());
    assertFalse(result.isWaitingForSecondPlayer());
  }

  @Test
  @DisplayName("creates more games if more than two players has registered")
  void registerPlayerMultiplePlayers() {
    Game first = service.registerPlayer(FIRST_PLAYER_NAME);
    Game second = service.registerPlayer(SECOND_PLAYER_NAME);
    Game third = service.registerPlayer(THIRD_PLAYER_NAME);

    assertEquals(first, second);
    assertNotEquals(second, third);
    assertEquals(THIRD_PLAYER_NAME, third.getFirstPlayer().getName());
  }

  @Test
  @DisplayName("retrieves a player from a valid token, null otherwise")
  void validateTokenExisting() {
    Game game = service.registerPlayer(FIRST_PLAYER_NAME);
    Player player = game.getFirstPlayer();
    String token = player.getToken();

    Player existing = service.validateToken(token);
    assertEquals(player, existing);

    Player notExisting = service.validateToken("anotherToken");
    assertNull(notExisting);
  }

  @Test
  @DisplayName("given a player, returns the game he/she's playing, throws an exception if nothing is found")
  void getGame() {
    Game game = service.registerPlayer(FIRST_PLAYER_NAME);
    Player player = game.getFirstPlayer();

    Game result = service.getGame(player);
    assertEquals(game, result);

    Player unexistingPlayer = new Player(SECOND_PLAYER_NAME);
    assertThrows(NoSuchElementException.class, () -> {
      service.getGame(unexistingPlayer);
    });
  }

  @Nested
  @DisplayName("when a move is made")
  class WhenAMoveIsMade {

    @Test
    @DisplayName("throws an exception if one of the coordinate is invalid")
    void moveInvalidCoordinate() {
      Game game = service.registerPlayer(FIRST_PLAYER_NAME);
      service.registerPlayer(SECOND_PLAYER_NAME);
      Player firstPlayer = game.getFirstPlayer();

      assertThrows(InvalidCoordinateException.class, () -> {
        service.move(firstPlayer, ROW, 4);
      });
    }

    @Test
    @DisplayName("throws an exception if a move is made on a game that's over")
    void moveOnOverGame() {
      Game game = service.registerPlayer(FIRST_PLAYER_NAME);
      service.registerPlayer(SECOND_PLAYER_NAME);
      Player firstPlayer = game.getFirstPlayer();
      game.setOver(true);

      assertThrows(IllegalMoveException.class, () -> {
        service.move(firstPlayer, ROW, COLUMN);
      });
    }

    @Test
    @DisplayName("throws an exception if the player that's making the move is not in turn")
    void moveNotInTurn() {
      Game game = service.registerPlayer(FIRST_PLAYER_NAME);
      service.registerPlayer(SECOND_PLAYER_NAME);
      Player secondPlayer = game.getSecondPlayer();

      assertThrows(IllegalMoveException.class, () -> {
        service.move(secondPlayer, ROW, COLUMN);
      });
    }

    @Test
    @DisplayName("throws an exception if the game is still waiting for the second player")
    void moveNotYetStarted() {
      Game game = service.registerPlayer(FIRST_PLAYER_NAME);
      Player firstPlayer = game.getFirstPlayer();

      assertThrows(IllegalMoveException.class, () -> {
        service.move(firstPlayer, ROW, COLUMN);
      });
    }

    @Test
    @DisplayName("throws an exception if the position is already occupied")
    void moveAlreadyOccupied() {
      Game game = service.registerPlayer(FIRST_PLAYER_NAME);
      service.registerPlayer(SECOND_PLAYER_NAME);
      Player firstPlayer = game.getFirstPlayer();
      game.getBoard().put(ROW, COLUMN);

      assertThrows(IllegalMoveException.class, () -> {
        service.move(firstPlayer, ROW, COLUMN);
      });
    }

    @Test
    @DisplayName("changes the game board and switch turn if the move is valid")
    void moveChangeBoardAndTurn() {
      Game game = service.registerPlayer(FIRST_PLAYER_NAME);
      service.registerPlayer(SECOND_PLAYER_NAME);
      Player firstPlayer = game.getFirstPlayer();
      Player secondPlayer = game.getSecondPlayer();

      assertTrue(game.getBoard().isClear());
      assertEquals(firstPlayer, game.getPlayerOnMove());

      Game result = service.move(firstPlayer, ROW, COLUMN);

      assertEquals(game, result);
      assertFalse(game.getBoard().isClear());
      assertFalse(game.getBoard().isEmpty(ROW, COLUMN));
      assertEquals(secondPlayer, game.getPlayerOnMove());
    }

    @ParameterizedTest
    @MethodSource("com.github.pixel13.tictactoe.service.impl.GameServiceImplTest#endMoveMethodSource")
    @DisplayName("sets the game over if it's a winning move or if the game is draw")
    void moveGameOver(List<Pair<Integer, Integer>> moves, Pair<Integer, Integer> move, boolean isWinning) {
      Game game = service.registerPlayer(FIRST_PLAYER_NAME);
      service.registerPlayer(SECOND_PLAYER_NAME);
      Player firstPlayer = game.getFirstPlayer();

      moves.forEach(m -> game.getBoard().put(m.getLeft(), m.getRight()));
      assertFalse(game.isOver());

      Game result = service.move(firstPlayer, move.getLeft(), move.getRight());

      assertEquals(game, result);
      assertTrue(game.isOver());
      Optional<Player> winner = game.getWinner();
      if (isWinning) {
        assertTrue(winner.isPresent());
        assertEquals(firstPlayer, winner.get());
        assertFalse(game.isDraw());
      } else {
        assertFalse(winner.isPresent());
        assertTrue(game.isDraw());
      }
    }
  }

  private static Stream<Arguments> endMoveMethodSource() {
    SequenceBuilder builder = SequenceBuilder.start().X(0, 0).O(1, 0).X(0, 2).O(2, 1).X(1, 1);
    return Stream.of(
        Arguments.of(builder.copy().O(1, 2).build(), new ImmutablePair<>(0, 1), true),
        Arguments.of(builder.copy().O(1, 2).build(), new ImmutablePair<>(2, 2), true),
        Arguments.of(builder.copy().O(1, 2).build(), new ImmutablePair<>(2, 0), true),
        Arguments.of(SequenceBuilder.start().X(0, 0).O(0, 1).X(1, 0).O(1, 1).build(), new ImmutablePair<>(2, 0), true),
        Arguments.of(
            SequenceBuilder.start()
                .X(0, 0)
                .O(0, 1)
                .X(0, 2)
                .O(1, 0)
                .X(1, 2)
                .O(2, 2)
                .X(2, 1)
                .O(1, 1)
                .build(),
            new ImmutablePair<>(2, 0),
            false
        )
    );
  }

  private static class SequenceBuilder {

    private List<Pair<Integer, Integer>> sequence = new ArrayList<>();

    private SequenceBuilder() {
    }

    public static SequenceBuilder start() {
      return new SequenceBuilder();
    }

    public SequenceBuilder then(int x, int y) {
      sequence.add(new ImmutablePair<>(x, y));
      return this;
    }

    public SequenceBuilder X(int x, int y) {
      return then(x, y);
    }

    public SequenceBuilder O(int x, int y) {
      return then(x, y);
    }

    public SequenceBuilder copy() {
      SequenceBuilder newInstance = SequenceBuilder.start();
      sequence.forEach(move -> newInstance.then(move.getLeft(), move.getRight()));
      return newInstance;
    }

    public List<Pair<Integer, Integer>> build() {
      return sequence;
    }
  }
}
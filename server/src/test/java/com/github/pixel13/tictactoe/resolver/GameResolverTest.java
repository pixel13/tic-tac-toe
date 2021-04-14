package com.github.pixel13.tictactoe.resolver;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.github.pixel13.tictactoe.domain.Board;
import com.github.pixel13.tictactoe.domain.Cell;
import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.domain.Player;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameResolver")
class GameResolverTest {

  private static final String PLAYER_NAME = "test";
  private static final String SIGN1 = "X";
  private static final String SIGN2 = "O";

  @Mock
  private Game mockGame;

  @Mock
  private Board mockBoard;

  @InjectMocks
  private GameResolver resolver;

  @Test
  @DisplayName("returns the name of the player who is on move")
  void getOnMove() {
    when(mockGame.getPlayerOnMove()).thenReturn(new Player(PLAYER_NAME));
    String result = resolver.getOnMove(mockGame);
    assertEquals(PLAYER_NAME, result);
  }

  @Test
  @DisplayName("returns the name of the winner, if any")
  void getWinnerPresent() {
    when(mockGame.getWinner()).thenReturn(Optional.of(new Player(PLAYER_NAME)));
    Optional<String> result = resolver.getWinner(mockGame);
    assertTrue(result.isPresent());
    assertEquals(PLAYER_NAME, result.get());
  }

  @Test
  @DisplayName("returns an empty optional if no winner is present")
  void getWinnerEmpty() {
    when(mockGame.getWinner()).thenReturn(Optional.empty());
    Optional<String> result = resolver.getWinner(mockGame);
    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("returns a list of cell as a representation of the game board")
  void board() {
    List<Cell> expected = List.of(new Cell(0, 0, SIGN1), new Cell(1, 1, SIGN2));
    when(mockGame.getBoard()).thenReturn(mockBoard);
    when(mockBoard.toCells(SIGN1, SIGN2)).thenReturn(expected);
    List<Cell> result = resolver.board(mockGame, SIGN1, SIGN2);
    assertEquals(expected, result);
  }

  @Test
  @DisplayName("returns that the game is not started if waiting for the second player")
  void isStartedFalse() {
    when(mockGame.isWaitingForSecondPlayer()).thenReturn(true);
    boolean result = resolver.isStarted(mockGame);
    assertFalse(result);
  }

  @Test
  @DisplayName("returns that the game is started if not waiting for the second player")
  void isStartedTrue() {
    when(mockGame.isWaitingForSecondPlayer()).thenReturn(false);
    boolean result = resolver.isStarted(mockGame);
    assertTrue(result);
  }
}
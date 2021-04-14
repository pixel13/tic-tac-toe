package com.github.pixel13.tictactoe.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.domain.Player;
import com.github.pixel13.tictactoe.security.AuthManager;
import com.github.pixel13.tictactoe.service.GameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Sinks.Many;

@ExtendWith(MockitoExtension.class)
@DisplayName("MutationResolver")
class MutationResolverTest {

  private static final String FIRST_PLAYER_NAME = "playerName";
  private static final String SECOND_PLAYER_NAME = "anotherPlayer";
  private static final Player FIRST_PLAYER = new Player(FIRST_PLAYER_NAME);
  private static final Player SECOND_PLAYER = new Player(SECOND_PLAYER_NAME);
  private static final int ROW = 1;
  private static final int COLUMN = 2;

  @Mock
  private GameService mockGameService;

  @Mock
  private AuthManager mockAuthManager;

  @Mock
  private Many<Game> mockGameSink;

  @InjectMocks
  private MutationResolver resolver;

  @Test
  @DisplayName("registers the first player of a game")
  void startGameForFirstPlayer() {
    Game game = new Game(FIRST_PLAYER);

    when(mockGameService.registerPlayer(FIRST_PLAYER_NAME)).thenReturn(game);

    Player result = resolver.startGame(FIRST_PLAYER_NAME);

    assertEquals(FIRST_PLAYER, result);
  }

  @Test
  @DisplayName("registers the second player of a game and sends data to the reactive stream")
  void startGameForSecondPlayer() {
    Game game = new Game(FIRST_PLAYER);
    game.setSecondPlayer(SECOND_PLAYER);

    when(mockGameService.registerPlayer(SECOND_PLAYER_NAME)).thenReturn(game);

    Player result = resolver.startGame(SECOND_PLAYER_NAME);

    assertEquals(SECOND_PLAYER, result);

    verify(mockGameSink).tryEmitNext(game);
  }

  @Test
  @DisplayName("make a move in a game and sends data to the reactive stream")
  void move() {
    Game game = new Game(FIRST_PLAYER);
    when(mockAuthManager.getCurrentPlayer()).thenReturn(FIRST_PLAYER);
    when(mockGameService.move(FIRST_PLAYER, ROW, COLUMN)).thenReturn(game);

    Game result = resolver.move(ROW, COLUMN);

    assertEquals(game, result);

    verify(mockGameSink).tryEmitNext(game);
  }
}
package com.github.pixel13.tictactoe.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("QueryResolver")
class QueryResolverTest {

  @Mock
  private GameService mockGameService;

  @Mock
  private AuthManager mockAuthManager;

  @InjectMocks
  private QueryResolver resolver;

  @Test
  @DisplayName("returns the game that's playing the current logged in user")
  void getGameStatus() {
    Player player = new Player("test");
    Game expected = new Game(player);
    when(mockAuthManager.getCurrentPlayer()).thenReturn(player);
    when(mockGameService.getGame(player)).thenReturn(expected);

    Game result = resolver.getGameStatus();

    assertEquals(expected, result);
  }
}
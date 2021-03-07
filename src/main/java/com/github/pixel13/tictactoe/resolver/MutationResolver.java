package com.github.pixel13.tictactoe.resolver;

import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.domain.Player;
import com.github.pixel13.tictactoe.security.AuthManager;
import com.github.pixel13.tictactoe.service.GameService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks.Many;

@Component
public class MutationResolver implements GraphQLMutationResolver {

  @Autowired
  private GameService gameService;

  @Autowired
  private AuthManager authManager;

  @Autowired
  private Many<Game> gameSink;

  public Player startGame(String playerName) {
    Game game = gameService.registerPlayer(playerName);

    if (game.isWaitingForSecondPlayer()) {
      return game.getFirstPlayer();
    }

    gameSink.tryEmitNext(game);
    return game.getSecondPlayer();
  }

  @PreAuthorize("isAuthenticated()")
  public Game move(int row, int column) {
    Game game = gameService.move(authManager.getCurrentPlayer(), row, column);
    gameSink.tryEmitNext(game);
    return game;
  }
}

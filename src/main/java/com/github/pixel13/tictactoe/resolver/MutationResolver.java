package com.github.pixel13.tictactoe.resolver;

import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.domain.Player;
import com.github.pixel13.tictactoe.security.AuthManager;
import com.github.pixel13.tictactoe.service.GameService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class MutationResolver implements GraphQLMutationResolver {

  @Autowired
  private GameService gameService;

  @Autowired
  private AuthManager authManager;

  public Player startGame(String playerName) {
    return gameService.registerPlayer(playerName);
  }

  @PreAuthorize("isAuthenticated()")
  public Game move(int row, int column) {
    return gameService.move(authManager.getCurrentPlayer(), row, column);
  }
}

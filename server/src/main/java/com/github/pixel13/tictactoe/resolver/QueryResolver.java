package com.github.pixel13.tictactoe.resolver;

import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.security.AuthManager;
import com.github.pixel13.tictactoe.service.GameService;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class QueryResolver implements GraphQLQueryResolver {

  @Autowired
  private GameService gameService;

  @Autowired
  private AuthManager authManager;

  @PreAuthorize("isAuthenticated()")
  public Game getGameStatus() {
    return gameService.getGame(authManager.getCurrentPlayer());
  }

}

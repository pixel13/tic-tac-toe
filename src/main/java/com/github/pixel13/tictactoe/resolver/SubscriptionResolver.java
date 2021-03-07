package com.github.pixel13.tictactoe.resolver;

import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.domain.Player;
import com.github.pixel13.tictactoe.security.AuthManager;
import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class SubscriptionResolver implements GraphQLSubscriptionResolver {

  @Autowired
  private Flux<Game> gameEvents;

  @Autowired
  private AuthManager authManager;

  @PreAuthorize("isAuthenticated()")
  public Publisher<String> opponentArrived() {
    Player currentPlayer = authManager.getCurrentPlayer();
    return gameEvents
        .filter(game -> game.isOnMove(currentPlayer))
        .filter(game -> game.getBoard().isClear())
        .map(game -> game.getSecondPlayer().getName());
  }

  @PreAuthorize("isAuthenticated()")
  public Publisher<Game> opponentMove() {
    Player currentPlayer = authManager.getCurrentPlayer();
    return gameEvents
        .filter(game -> game.isOnMove(currentPlayer))
        .filter(game -> !game.getBoard().isClear());
  }

}

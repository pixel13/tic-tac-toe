package com.github.pixel13.tictactoe.resolver;

import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.domain.Player;
import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionResolver implements GraphQLSubscriptionResolver {

  public boolean opponentArrived() {
    return false;
  }

  public Game opponentMove() {
    return new Game(new Player(""));
  }

}

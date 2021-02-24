package com.github.pixel13.tictactoe.resolver;

import com.github.pixel13.tictactoe.domain.Cell;
import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.domain.Player;
import graphql.kickstart.tools.GraphQLResolver;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GameResolver implements GraphQLResolver<Game> {

  public Player getOnMove(Game game) {
    return game.getPlayerOnMove();
  }

  public List<Cell> board(Game game, String firstPlayerSign, String secondPlayerSign) {
    return game.getBoard().toCells(firstPlayerSign, secondPlayerSign);
  }

  public boolean isStarted(Game game) {
    return !game.isWaitingForSecondPlayer();
  }

}

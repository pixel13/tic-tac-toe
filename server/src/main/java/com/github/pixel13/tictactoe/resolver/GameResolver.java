package com.github.pixel13.tictactoe.resolver;

import com.github.pixel13.tictactoe.domain.Cell;
import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.domain.Player;
import graphql.kickstart.tools.GraphQLResolver;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class GameResolver implements GraphQLResolver<Game> {

  public String getOnMove(Game game) {
    return game.getPlayerOnMove().getName();
  }

  public Optional<String> getWinner(Game game) {
    return game.getWinner().map(Player::getName);
  }

  public List<Cell> board(Game game, String firstPlayerSign, String secondPlayerSign) {
    return game.getBoard().toCells(firstPlayerSign, secondPlayerSign);
  }

  public boolean isReady(Game game) {
    return !game.isWaitingForSecondPlayer();
  }

}

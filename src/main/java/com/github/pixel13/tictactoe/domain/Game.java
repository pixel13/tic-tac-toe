package com.github.pixel13.tictactoe.domain;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public class Game {

  public static final int SIZE = 3;

  @Getter
  private final String id = UUID.randomUUID().toString();

  private boolean isFirstPlayerOnMove = true;

  @Getter
  @Setter
  private boolean isOver = false;

  @Setter
  private boolean isDraw = false;

  private final Player firstPlayer;

  @Setter
  private Player secondPlayer;

  @Getter
  private final Board board = new Board(SIZE);

  public Game(Player player) {
    this.firstPlayer = player;
  }

  public boolean isWaitingForSecondPlayer() {
    return secondPlayer == null;
  }

  public boolean hasPlayer(Player player) {
    return firstPlayer.equals(player) || secondPlayer.equals(player);
  }

  public boolean isOnMove(Player player) {
    return getPlayerOnMove().equals(player);
  }

  public void changeTurn() {
    isFirstPlayerOnMove = !isFirstPlayerOnMove;
  }

  public Player getPlayerOnMove() {
    return isFirstPlayerOnMove ? firstPlayer : secondPlayer;
  }

  public Player getWinner() {
    if (!isOver || isDraw) {
      return null;
    }

    return getPlayerOnMove();
  }
}

package com.github.pixel13.tictactoe.domain;

import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Game {

  public static final int SIZE = 3;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private boolean isFirstPlayerOnMove = true;
  private final String id = UUID.randomUUID().toString();
  private boolean isOver = false;
  private boolean isDraw = false;
  private final Player firstPlayer;
  private Player secondPlayer;
  private final Board board = new Board(SIZE);

  public Game(Player player) {
    this.firstPlayer = player;
  }

  public boolean isWaitingForSecondPlayer() {
    return secondPlayer == null;
  }

  public boolean hasPlayer(Player player) {
    return player.equals(firstPlayer) || player.equals(secondPlayer);
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

  public Optional<Player> getWinner() {
    if (!isOver || isDraw) {
      return Optional.empty();
    }

    return Optional.of(getLastMovePlayer());
  }

  private Player getLastMovePlayer() {
    return isFirstPlayerOnMove ? secondPlayer : firstPlayer;
  }
}

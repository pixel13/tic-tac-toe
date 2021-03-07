package com.github.pixel13.tictactoe.service.impl;

import static com.github.pixel13.tictactoe.domain.Game.SIZE;

import com.github.pixel13.tictactoe.domain.Board;
import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.domain.Player;
import com.github.pixel13.tictactoe.exception.IllegalMoveException;
import com.github.pixel13.tictactoe.exception.InvalidCoordinateException;
import com.github.pixel13.tictactoe.service.GameService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class GameServiceImpl implements GameService {

  private final Map<String, Player> players = new HashMap<>();
  private final Map<String, Game> games = new HashMap<>();

  @Override
  public Game registerPlayer(String name) {
    return assignToGame(newPlayer(name));
  }

  @Override
  public Player validateToken(String token) {
    return players.get(token);
  }

  @Override
  public Game getGame(Player player) {
    return games.values().stream()
        .filter(game -> game.hasPlayer(player))
        .findAny()
        .orElseThrow();
  }

  @Override
  public Game move(Player player, int x, int y) {
    checkCoordinate(x);
    checkCoordinate(y);

    Game game = getGame(player);
    checkPlayerCanMove(player, game);

    Board board = game.getBoard();
    move(x, y, board);

    if (isWinningMove(x, y, board)) {
      game.setOver(true);
    } else if (isDraw(board)) {
      game.setOver(true);
      game.setDraw(true);
    }

    game.changeTurn();
    return game;
  }

  private void checkPlayerCanMove(Player player, Game game) {
    if (game.isOver()) {
      throw new IllegalMoveException("Game is over");
    }

    if (!game.isOnMove(player)) {
      throw new IllegalMoveException("Player is not on move");
    }

    if (game.isWaitingForSecondPlayer()) {
      throw new IllegalMoveException("The game is not started yet");
    }
  }

  private void move(int x, int y, Board board) {
    if (!board.isEmpty(x, y)) {
      throw new IllegalMoveException("Cell is already occupied");
    }

    board.put(x, y);
  }

  private boolean isDraw(Board board) {
    return IntStream.range(0, SIZE).noneMatch(x -> IntStream.range(0, SIZE).anyMatch(y -> board.get(x, y) == null));
  }

  private boolean isWinningMove(int x, int y, Board board) {
    Boolean value = board.get(x, y);

    // Horizontal line
    if (IntStream.range(0, SIZE).allMatch(row -> hasValue(board.get(row, y), value))) {
      return true;
    }

    // Vertical line
    if (IntStream.range(0, SIZE).allMatch(column -> hasValue(board.get(x, column), value))) {
      return true;
    }

    // Top left-bottom right diagonal
    if (x == y && IntStream.range(0, SIZE).allMatch(i -> hasValue(board.get(i, i), value))) {
      return true;
    }

    // Bottom left-top right diagonal
    if (x + y == SIZE - 1 && IntStream.range(0, SIZE).allMatch(i -> hasValue(board.get(i, SIZE - 1 - i), value))) {
      return true;
    }

    return false;
  }

  private boolean hasValue(Boolean testing, boolean value) {
    return Optional.ofNullable(testing)
        .map(v -> v== value)
        .orElse(false);
  }

  private void checkCoordinate(int value) {
    if (value < 0 || value > SIZE) {
      throw new InvalidCoordinateException(value);
    }
  }

  private Player newPlayer(String name) {
    Player player = new Player(name);
    players.put(player.getToken(), player);
    return player;
  }

  private Game assignToGame(Player player) {
    return games.values().stream()
        .filter(Game::isWaitingForSecondPlayer)
        .findAny()
        .map(game -> setSecondPlayer(game, player))
        .orElse(newGame(player));
  }

  private Game setSecondPlayer(Game game, Player player) {
    game.setSecondPlayer(player);
    return game;
  }

  private Game newGame(Player player) {
    Game game = new Game(player);
    games.put(game.getId(), game);
    return game;
  }

}

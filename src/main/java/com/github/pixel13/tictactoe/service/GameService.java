package com.github.pixel13.tictactoe.service;

import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.domain.Player;

public interface GameService {

  Game registerPlayer(String name);

  Player validateToken(String token);

  Game move(Player player, int row, int column);

  Game getGame(Player player);

}

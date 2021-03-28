package com.github.pixel13.tictactoe.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TicTacToe {

  public static void main(String[] args) throws IOException {
    Properties props = new Properties();
    try (InputStream resourceStream = TicTacToe.class.getClassLoader().getResourceAsStream("application.properties")) {
      props.load(resourceStream);
    }

    Game game = new Game(props);
    game.startNewGame();
  }

}

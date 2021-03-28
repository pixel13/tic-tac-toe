package com.github.pixel13.tictactoe.exception;

public class IllegalMoveException extends RuntimeException {

  private static final long serialVersionUID = 6196667112542245859L;

  public IllegalMoveException(String reason) {
    super("Illegal move: " + reason);
  }

}

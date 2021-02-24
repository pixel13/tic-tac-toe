package com.github.pixel13.tictactoe.exception;

public class InvalidCoordinateException extends RuntimeException {

  private static final long serialVersionUID = 6196667112542245859L;

  public InvalidCoordinateException(int value) {
    super("Invalid coordinate " + value + ": must be a number between 1 and 3");
  }

}

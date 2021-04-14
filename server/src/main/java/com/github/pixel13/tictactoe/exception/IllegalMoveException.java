package com.github.pixel13.tictactoe.exception;

import java.util.Collections;
import java.util.Map;

public class IllegalMoveException extends GraphQLExecutionException {

  private static final long serialVersionUID = 6196667112542245859L;

  public IllegalMoveException(String reason) {
    super("Illegal move: " + reason);
  }

  @Override
  public Map<String, Object> getExtensions() {
    return Collections.singletonMap(ERROR_CODE_EXTENSION, ERROR_CODE_ILLEGAL_MOVE);
  }

}

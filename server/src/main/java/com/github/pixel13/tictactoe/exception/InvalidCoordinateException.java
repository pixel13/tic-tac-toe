package com.github.pixel13.tictactoe.exception;

import java.util.Collections;
import java.util.Map;

public class InvalidCoordinateException extends GraphQLExecutionException {

  private static final long serialVersionUID = 6196667112542245859L;

  public InvalidCoordinateException(int value) {
    super("Invalid coordinate " + value + ": must be a number between 1 and 3");
  }

  @Override
  public Map<String, Object> getExtensions() {
    return Collections.singletonMap(ERROR_CODE_EXTENSION, ERROR_CODE_INVALID_COORDINATE);
  }

}

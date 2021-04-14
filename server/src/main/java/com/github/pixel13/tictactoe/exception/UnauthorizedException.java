package com.github.pixel13.tictactoe.exception;

import java.util.Collections;
import java.util.Map;

public class UnauthorizedException extends GraphQLExecutionException {

  private static final long serialVersionUID = -5556206905686318471L;

  public UnauthorizedException() {
    super("Unauthorized");
  }

  @Override
  public Map<String, Object> getExtensions() {
    return Collections.singletonMap(ERROR_CODE_EXTENSION, ERROR_CODE_UNAUTHORIZED);
  }

}

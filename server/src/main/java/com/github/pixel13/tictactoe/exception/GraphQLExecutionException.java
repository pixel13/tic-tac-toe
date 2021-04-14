package com.github.pixel13.tictactoe.exception;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import java.util.Collections;
import java.util.List;

public abstract class GraphQLExecutionException extends RuntimeException implements GraphQLError {

  public static final String ERROR_CODE_EXTENSION = "ERROR_CODE";
  public static final String ERROR_CODE_UNAUTHORIZED = "UNAUTHORIZED";
  public static final String ERROR_CODE_ILLEGAL_MOVE = "ILLEGAL_MOVE";
  public static final String ERROR_CODE_INVALID_COORDINATE = "INVALID_COORDINATE";

  protected GraphQLExecutionException() {
  }

  protected GraphQLExecutionException(String message) {
    super(message);
  }

  protected GraphQLExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  protected GraphQLExecutionException(Throwable cause) {
    super(cause);
  }

  @Override
  public List<SourceLocation> getLocations() {
    return Collections.emptyList();
  }

  @Override
  public ErrorClassification getErrorType() {
    return null;
  }

}

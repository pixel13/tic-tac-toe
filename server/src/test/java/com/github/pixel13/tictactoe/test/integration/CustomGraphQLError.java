package com.github.pixel13.tictactoe.test.integration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.pixel13.tictactoe.exception.GraphQLExecutionException;
import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CustomGraphQLError implements GraphQLError {

  private String message;
  @JsonIgnore
  private List<SourceLocation> locations;
  private ErrorClassification errorType;
  private List<Object> path;
  private Map<String, Object> extensions;

  public String getErrorCode() {
    return (String) getExtensions().get(GraphQLExecutionException.ERROR_CODE_EXTENSION);
  }
}

package com.github.pixel13.tictactoe.exception.handler;

import com.github.pixel13.tictactoe.exception.GraphQLExecutionException;
import com.github.pixel13.tictactoe.exception.UnauthorizedException;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.GraphqlErrorException;
import graphql.execution.ResultPath;
import graphql.kickstart.spring.error.ErrorContext;
import graphql.kickstart.spring.error.ThrowableGraphQLError;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Component
public class GraphQLExceptionHandler {

  @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
  public GraphQLError handleAccessDeniedException(Exception e) {
    return new UnauthorizedException();
  }

  @ExceptionHandler(GraphQLExecutionException.class)
  public GraphQLError handleDataFetchingException(GraphQLExecutionException e, ErrorContext context) {
    return new ExceptionWhileDataFetching(ResultPath.fromList(context.getPath()), e, context.getLocations().get(0));
  }

  @ExceptionHandler(GraphqlErrorException.class)
  public GraphQLError handleGraphQLException(GraphqlErrorException e) {
    return e;
  }

  @ExceptionHandler(Throwable.class)
  public GraphQLError handleInternalExceptions(Throwable e) {
    return new ThrowableGraphQLError(e, "Internal server error");
  }

}

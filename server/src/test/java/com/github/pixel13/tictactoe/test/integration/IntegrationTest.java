package com.github.pixel13.tictactoe.test.integration;

import static com.github.pixel13.tictactoe.exception.GraphQLExecutionException.ERROR_CODE_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.pixel13.tictactoe.domain.Player;
import com.github.pixel13.tictactoe.service.GameService;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestSubscription;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class IntegrationTest {

  protected static final String FIRST_PLAYER_NAME = "firstPlayer";
  protected static final String SECOND_PLAYER_NAME = "secondPlayer";
  protected static final long MAX_SUBSCRIPTION_TIMEOUT = 2L;

  @Autowired
  protected GraphQLTestTemplate graphQLTestTemplate;

  @Autowired
  protected GraphQLTestSubscription graphQLTestSubscription;

  @Autowired
  protected GameService gameService;

  protected void assertUnauthorized(GraphQLResponse response) {
    assertGraphQLError(response, ERROR_CODE_UNAUTHORIZED);
  }

  protected void assertGraphQLError(GraphQLResponse response, String error) {
    assertTrue(response.isOk());
    CustomGraphQLError[] errors = response.get("$.errors", CustomGraphQLError[].class);
    assertEquals(1, errors.length);
    assertEquals(error, errors[0].getErrorCode());
  }

  protected AuthenticatedGraphQLTestTemplate registerPlayer(String name) {
    String token = Optional.of(gameService.registerPlayer(name))
        .map(game -> Optional.ofNullable(game.getSecondPlayer()).orElse(game.getFirstPlayer()))
        .map(Player::getToken)
        .orElse(null);

    return new AuthenticatedGraphQLTestTemplate(graphQLTestTemplate, token);
  }

  protected ObjectNode variable(String name, String value) {
    ObjectNode variables = new ObjectMapper().createObjectNode();
    variables.put(name, value);
    return variables;
  }

  protected GraphQLResponse move(AuthenticatedGraphQLTestTemplate graphQLTestTemplate, int x, int y) throws IOException {
    ObjectNode variables = new ObjectMapper().createObjectNode();
    variables.put("row", x);
    variables.put("column", y);
    return graphQLTestTemplate.perform("graphql/mutation/move.graphql", variables);
  }

  protected GraphQLResponse move(GraphQLTestTemplate graphQLTestTemplate, int x, int y) throws IOException {
    ObjectNode variables = new ObjectMapper().createObjectNode();
    variables.put("row", x);
    variables.put("column", y);
    return graphQLTestTemplate.perform("graphql/mutation/move.graphql", variables);
  }

  public static class AuthenticatedGraphQLTestTemplate {

    private final String token;
    private final GraphQLTestTemplate testTemplate;

    public AuthenticatedGraphQLTestTemplate(GraphQLTestTemplate graphQLTestTemplate, String token) {
      this.testTemplate = graphQLTestTemplate;
      this.token = token;
    }

    public GraphQLResponse perform(String graphqlResource, ObjectNode variables) throws IOException {
      return testTemplate.withBearerAuth(token).perform(graphqlResource, variables);
    }

    public GraphQLResponse postForResource(String graphqlResource) throws IOException {
      return testTemplate.withBearerAuth(token).postForResource(graphqlResource);
    }

    public String getToken() {
      return token;
    }
  }
}

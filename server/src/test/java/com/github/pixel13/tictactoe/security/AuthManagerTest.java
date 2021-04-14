package com.github.pixel13.tictactoe.security;

import static com.github.pixel13.tictactoe.test.AuthenticationExtension.TEST_PLAYER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.pixel13.tictactoe.domain.Player;
import com.github.pixel13.tictactoe.test.AuthenticationExtension;
import com.github.pixel13.tictactoe.test.WithAuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AuthenticationExtension.class)
@DisplayName("AuthManager")
class AuthManagerTest {

  private AuthManager authManager;

  @BeforeEach
  void setUp() {
    authManager = new AuthManager();
  }

  @Test
  @DisplayName("returns null if no user is logged in")
  void getCurrentPlayerNoUser() {
    assertNull(authManager.getCurrentPlayer());
  }

  @Test
  @WithAuthenticatedUser
  @DisplayName("returns current player if a user is logged in")
  void getCurrentPlayerUserAuthenticated() {
    Player result = authManager.getCurrentPlayer();
    assertEquals(TEST_PLAYER_NAME, result.getName());
    assertFalse(result.getToken().isEmpty());
  }
}
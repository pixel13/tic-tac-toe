package com.github.pixel13.tictactoe.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.github.pixel13.tictactoe.domain.Player;
import com.github.pixel13.tictactoe.service.GameService;
import com.github.pixel13.tictactoe.test.AuthenticationExtension;
import com.github.pixel13.tictactoe.test.WithAuthenticatedUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(AuthenticationExtension.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("Authenticator")
class AuthenticatorTest {

  private static final String TOKEN = "123123123";

  @Mock
  private GameService mockGameService;

  @InjectMocks
  private Authenticator authenticator;

  @Test
  @WithAuthenticatedUser
  @DisplayName("returns that current user is authenticated if an authentication object exists")
  void isAuthenticatedTrue() {
    assertTrue(authenticator.isAuthenticated());
  }

  @Test
  @DisplayName("returns that current user is not authenticated if an authentication object does not exist")
  void isAuthenticatedFalse() {
    assertFalse(authenticator.isAuthenticated());
  }

  @Test
  @DisplayName("authenticates a user using a valid Authorization header")
  void doAuthenticateSuccess() {
    Player player = new Player("test");
    when(mockGameService.validateToken(TOKEN)).thenReturn(player);

    assertFalse(authenticator.isAuthenticated());
    authenticator.doAuthenticate(Authenticator.BEARER + TOKEN);
    assertTrue(authenticator.isAuthenticated());
  }

  @Test
  @DisplayName("doesn't authenticate the user if no Authentication header is passed")
  void doAuthenticateNullHeader() {
    assertFalse(authenticator.isAuthenticated());
    authenticator.doAuthenticate(null);
    assertFalse(authenticator.isAuthenticated());
  }

  @Test
  @DisplayName("doesn't authenticate the user if the given header is not a Bearer Authentication header")
  void doAuthenticateNoBearerHeader() {
    assertFalse(authenticator.isAuthenticated());
    authenticator.doAuthenticate(TOKEN);
    assertFalse(authenticator.isAuthenticated());
  }

  @Test
  @DisplayName("doesn't authenticate the user if the Authentication header doesn't contain a valid token")
  void doAuthenticateNoValidToken() {
    assertFalse(authenticator.isAuthenticated());
    authenticator.doAuthenticate(Authenticator.BEARER + TOKEN);
    assertFalse(authenticator.isAuthenticated());
  }
}
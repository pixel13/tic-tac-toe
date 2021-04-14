package com.github.pixel13.tictactoe.test;

import static com.github.pixel13.tictactoe.security.Authenticator.ROLE_AUTHENTICATED;

import com.github.pixel13.tictactoe.domain.Player;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class AuthenticationExtension implements BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback {

  public static final String TEST_PLAYER_NAME = "testPlayer";

  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    setAuthentication(null);
  }

  @Override
  public void beforeTestExecution(ExtensionContext extensionContext) {
    extensionContext.getElement()
        .flatMap(el -> Optional.ofNullable(el.getAnnotation(WithAuthenticatedUser.class)))
        .ifPresent(this::setAuthenticatedUser);
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) {
    setAuthentication(null);
  }

  private void setAuthenticatedUser(WithAuthenticatedUser annotation) {
    Player player = new Player(TEST_PLAYER_NAME);
    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(ROLE_AUTHENTICATED));
    PreAuthenticatedAuthenticationToken auth = new PreAuthenticatedAuthenticationToken(player, player.getToken(), authorities);
    setAuthentication(auth);
  }

  private void setAuthentication(Authentication auth) {
    SecurityContextHolder.getContext().setAuthentication(auth);
  }
}

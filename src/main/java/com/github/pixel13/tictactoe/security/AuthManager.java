package com.github.pixel13.tictactoe.security;

import com.github.pixel13.tictactoe.domain.Player;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AuthManager {

  public Player getCurrentPlayer() {
    return Optional.ofNullable(getAuthToken())
        .map(auth -> (Player) auth.getPrincipal())
        .orElse(null);
  }

  private PreAuthenticatedAuthenticationToken getAuthToken() {
    return (PreAuthenticatedAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
  }

}

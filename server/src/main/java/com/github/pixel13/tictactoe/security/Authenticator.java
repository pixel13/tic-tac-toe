package com.github.pixel13.tictactoe.security;

import com.github.pixel13.tictactoe.service.GameService;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class Authenticator {

  public static final String ROLE_AUTHENTICATED = "AUTHENTICATED";
  static final String BEARER = "Bearer ";
  private static final Collection<GrantedAuthority> AUTHORITIES = Collections.singleton(new SimpleGrantedAuthority(ROLE_AUTHENTICATED));

  @Autowired
  private GameService gameService;

  public boolean isAuthenticated() {
    return (SecurityContextHolder.getContext().getAuthentication() instanceof PreAuthenticatedAuthenticationToken);
  }

  public void doAuthenticate(String authorizationHeader) {
    Optional.ofNullable(authorizationHeader)
        .filter(header -> header.startsWith(BEARER))
        .map(header -> header.substring(BEARER.length()))
        .ifPresent(this::authenticate);
  }

  private void authenticate(String token) {
    Optional.ofNullable(gameService.validateToken(token))
        .map(player -> new PreAuthenticatedAuthenticationToken(player, token, AUTHORITIES))
        .ifPresent(SecurityContextHolder.getContext()::setAuthentication);
  }

}

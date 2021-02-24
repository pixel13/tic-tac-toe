package com.github.pixel13.tictactoe.security;

import com.github.pixel13.tictactoe.service.GameService;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class TokenAuthorizationFilter extends BasicAuthenticationFilter {

  private static final String BEARER = "Bearer ";
  private static final String ROLE_AUTHENTICATED = "AUTHENTICATED";
  private static final Collection<GrantedAuthority> AUTHORITIES = Collections.singleton(new SimpleGrantedAuthority(ROLE_AUTHENTICATED));
  private final GameService gameService;

  public TokenAuthorizationFilter(AuthenticationManager authenticationManager, GameService gameService) {
    super(authenticationManager);
    this.gameService = gameService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
        .filter(header -> header.startsWith(BEARER))
        .map(header -> header.substring(BEARER.length()))
        .ifPresent(this::authenticate);

    chain.doFilter(request, response);
  }

  private void authenticate(String token) {
    Optional.ofNullable(gameService.validateToken(token))
        .map(player -> new PreAuthenticatedAuthenticationToken(player, token, AUTHORITIES))
        .ifPresent(SecurityContextHolder.getContext()::setAuthentication);
  }
}

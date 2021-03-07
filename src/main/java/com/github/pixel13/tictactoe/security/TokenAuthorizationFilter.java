package com.github.pixel13.tictactoe.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class TokenAuthorizationFilter extends BasicAuthenticationFilter {

  private final Authenticator authenticator;

  public TokenAuthorizationFilter(AuthenticationManager authenticationManager, Authenticator authenticator) {
    super(authenticationManager);
    this.authenticator = authenticator;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    authenticator.doAuthenticate(request.getHeader(HttpHeaders.AUTHORIZATION));
    chain.doFilter(request, response);
  }

}

package com.github.pixel13.tictactoe.security;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenAuthorizationFilter")
class TokenAuthorizationFilterTest {

  private static final String HEADER_VALUE = "Bearer 123123123";

  @Mock
  private Authenticator mockAuthenticator;

  @Mock
  private AuthenticationManager mockAuthenticationManager;

  @Mock
  private HttpServletRequest mockRequest;

  @Mock
  private HttpServletResponse mockResponse;

  @Mock
  private FilterChain mockFilterChain;

  private TokenAuthorizationFilter filter;

  @BeforeEach
  void setUp() {
    filter = new TokenAuthorizationFilter(mockAuthenticationManager, mockAuthenticator);
  }

  @Test
  @DisplayName("tries to authenticate using the Authentication header in the request")
  void doFilterInternal() throws IOException, ServletException {
    when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(HEADER_VALUE);
    filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
    verify(mockAuthenticator).doAuthenticate(HEADER_VALUE);
    verify(mockFilterChain).doFilter(mockRequest, mockResponse);
  }
}
package com.github.pixel13.tictactoe.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.kickstart.execution.subscriptions.SubscriptionSession;
import graphql.kickstart.execution.subscriptions.apollo.OperationMessage;
import java.util.Collections;
import java.util.Map;
import javax.websocket.server.HandshakeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionConnectionListener")
class SubscriptionConnectionListenerTest {

  private static final String HEADER_VALUE = "Bearer 123123123";
  @Mock
  private Authenticator mockAuthenticator;

  @Mock
  private SubscriptionSession mockSubscriptionSession;

  @Mock
  private OperationMessage mockOperationMessage;

  @Mock
  private HandshakeRequest mockHandshakeRequest;

  @InjectMocks
  private SubscriptionConnectionListener listener;

  @Test
  @DisplayName("does nothing on connect if no payload is provided")
  void onConnectNoPayload() {
    listener.onConnect(mockSubscriptionSession, mockOperationMessage);
    verify(mockAuthenticator, times(0)).doAuthenticate(anyString());
  }

  @Test
  @DisplayName("tries to authenticate the user on connect using the message payload, if provided")
  void onConnectWithPayload() {
    Map<String, String> payload = Map.of(HttpHeaders.AUTHORIZATION, HEADER_VALUE);
    when(mockOperationMessage.getPayload()).thenReturn(payload);
    listener.onConnect(mockSubscriptionSession, mockOperationMessage);
    verify(mockAuthenticator).doAuthenticate(HEADER_VALUE);
  }

  @Test
  @DisplayName("does nothing on start if an authentication already exists")
  void onStartAlreadyAuthenticated() {
    when(mockAuthenticator.isAuthenticated()).thenReturn(true);
    listener.onStart(mockSubscriptionSession, mockOperationMessage);
    verify(mockAuthenticator, times(0)).doAuthenticate(anyString());
  }

  @Test
  @DisplayName("does nothing on start if no handshake request is found")
  void onStartNoHandshake() {
    when(mockSubscriptionSession.getUserProperties()).thenReturn(Collections.emptyMap());
    listener.onStart(mockSubscriptionSession, mockOperationMessage);
    verify(mockAuthenticator, times(0)).doAuthenticate(anyString());
  }

  @Test
  @DisplayName("does nothing on start if no Authorization header is found in the handshake request")
  void onStartNoAuthorizationHeader() {
    when(mockHandshakeRequest.getHeaders()).thenReturn(Collections.emptyMap());
    Map<String, Object> userProperties = Map.of(HandshakeRequest.class.getName(), mockHandshakeRequest);
    when(mockSubscriptionSession.getUserProperties()).thenReturn(userProperties);
    listener.onStart(mockSubscriptionSession, mockOperationMessage);
    verify(mockAuthenticator, times(0)).doAuthenticate(anyString());
  }

  @Test
  @DisplayName("tries to authenticate on start using the Authentication header in the handshake request")
  void onStartAuthenticate() {
    when(mockHandshakeRequest.getHeaders()).thenReturn(Map.of(HttpHeaders.AUTHORIZATION, Collections.singletonList(HEADER_VALUE)));
    Map<String, Object> userProperties = Map.of(HandshakeRequest.class.getName(), mockHandshakeRequest);
    when(mockSubscriptionSession.getUserProperties()).thenReturn(userProperties);
    listener.onStart(mockSubscriptionSession, mockOperationMessage);
    verify(mockAuthenticator).doAuthenticate(HEADER_VALUE);
  }

}
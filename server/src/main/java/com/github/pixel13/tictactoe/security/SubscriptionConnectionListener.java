package com.github.pixel13.tictactoe.security;

import graphql.kickstart.execution.subscriptions.SubscriptionSession;
import graphql.kickstart.execution.subscriptions.apollo.ApolloSubscriptionConnectionListener;
import graphql.kickstart.execution.subscriptions.apollo.OperationMessage;
import java.util.Map;
import java.util.Optional;
import javax.websocket.server.HandshakeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionConnectionListener implements ApolloSubscriptionConnectionListener {

  @Autowired
  private Authenticator authenticator;

  @Override
  @SuppressWarnings("unchecked")
  public void onConnect(SubscriptionSession session, OperationMessage message) {
    Map<String, String> payload = (Map<String, String>) message.getPayload();
    if (payload != null) {
      authenticator.doAuthenticate(payload.get(HttpHeaders.AUTHORIZATION));
    }
  }

  @Override
  public void onStart(SubscriptionSession session, OperationMessage message) {
    if (!authenticator.isAuthenticated()) {
      HandshakeRequest request = (HandshakeRequest) session.getUserProperties().get(HandshakeRequest.class.getName());
      Optional.ofNullable(request)
          .map(HandshakeRequest::getHeaders)
          .map(headers -> headers.get(HttpHeaders.AUTHORIZATION))
          .filter(list -> !list.isEmpty())
          .map(list -> list.get(0))
          .ifPresent(authHeaderValue -> authenticator.doAuthenticate(authHeaderValue));
    }
  }
}

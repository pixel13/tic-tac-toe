package com.github.pixel13.tictactoe.security;

import graphql.kickstart.execution.subscriptions.SubscriptionSession;
import graphql.kickstart.execution.subscriptions.apollo.ApolloSubscriptionConnectionListener;
import graphql.kickstart.execution.subscriptions.apollo.OperationMessage;
import java.util.Map;
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
    authenticator.doAuthenticate(payload.get(HttpHeaders.AUTHORIZATION));
  }
}

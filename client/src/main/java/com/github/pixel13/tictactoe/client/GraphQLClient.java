package com.github.pixel13.tictactoe.client;

import com.apollographql.apollo.ApolloCall.Callback;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Subscription;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport.Factory;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class GraphQLClient {

  private static final String HEADER_AUTHORIZATION = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final ApolloClient apolloClient;
  private String authToken;

  public GraphQLClient(String serverEndpoint, String subscriptionEndpoint) {

    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

    OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
        .minWebSocketMessageToCompress(0)
        .addInterceptor(this::authInterceptor)
        .build();

    apolloClient = ApolloClient.builder()
        .serverUrl(serverEndpoint)
        .subscriptionTransportFactory(new Factory(subscriptionEndpoint, okHttpClient))
        .okHttpClient(okHttpClient)
        .build();
  }

  public <T> CompletableFuture<T> query(Query<?, Optional<T>, ?> query) {
    CompletableFuture<T> future = new CompletableFuture<>();

    apolloClient
        .query(query)
        .enqueue(new Callback<>() {
          @Override
          public void onResponse(@NotNull com.apollographql.apollo.api.Response<Optional<T>> response) {
            completeFromResponse(future, response);
          }

          @Override
          public void onFailure(@NotNull ApolloException e) {
            future.completeExceptionally(e);
          }
        });

    return future;
  }

  public <T> CompletableFuture<T> mutate(Mutation<?, Optional<T>, ?> mutation) {
    CompletableFuture<T> future = new CompletableFuture<>();

    apolloClient
        .mutate(mutation)
        .enqueue(new Callback<>() {
          @Override
          public void onResponse(@NotNull com.apollographql.apollo.api.Response<Optional<T>> response) {
            completeFromResponse(future, response);
          }

          @Override
          public void onFailure(@NotNull ApolloException e) {
            future.completeExceptionally(e);
          }
        });

    return future;
  }

  public <T> CompletableFuture<T> subscribeForOneValue(Subscription<?, Optional<T>, ?> subscription) {
    CompletableFuture<T> future = new CompletableFuture<>();

    ApolloSubscriptionCall<Optional<T>> subscriptionCall = apolloClient.subscribe(subscription);
    subscriptionCall.execute(new ApolloSubscriptionCall.Callback<>() {
      @Override
      public void onResponse(@NotNull com.apollographql.apollo.api.Response<Optional<T>> response) {
        completeFromResponse(future, response);
        subscriptionCall.cancel();
      }

      @Override
      public void onFailure(@NotNull ApolloException e) {
        future.completeExceptionally(e);
        subscriptionCall.cancel();
      }

      @Override
      public void onCompleted() {
        // Do nothing
      }

      @Override
      public void onTerminated() {
        // Do nothing
      }

      @Override
      public void onConnected() {
        // Do nothing
      }
    });

    return future;
  }

  public <T> ApolloSubscriptionCall<Optional<T>> subscribe(Subscription<?, Optional<T>, ?> subscription, Consumer<T> consumer) {
    ApolloSubscriptionCall<Optional<T>> subscriptionCall = apolloClient.subscribe(subscription);
    subscriptionCall.execute(new ApolloSubscriptionCall.Callback<>() {
      @Override
      public void onResponse(@NotNull com.apollographql.apollo.api.Response<Optional<T>> response) {
        if (response.getData().isEmpty()) {
          throw new RuntimeException("Empty response");
        }

        consumer.accept(response.getData().get());
      }

      @Override
      public void onFailure(@NotNull ApolloException e) {
        throw new RuntimeException(e);
      }

      @Override
      public void onCompleted() {
        // Do nothing
      }

      @Override
      public void onTerminated() {
        // Do nothing
      }

      @Override
      public void onConnected() {
        // Do nothing
      }
    });

    return subscriptionCall;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  private <T> void completeFromResponse(CompletableFuture<T> future, @NotNull com.apollographql.apollo.api.Response<Optional<T>> response) {
    if (response.hasErrors()) {
      String errors = Objects.requireNonNull(response.getErrors()).stream().map(Error::toString).collect(Collectors.joining(", "));
      future.completeExceptionally(new ApolloException(errors));
      return;
    }

    Optional<T> data = response.getData();
    if (data.isEmpty()) {
      future.completeExceptionally(new RuntimeException("Unexpected empty data"));
      return;
    }

    future.complete(data.get());
  }

  private Response authInterceptor(Chain chain) throws IOException {
    Builder requestBuilder = chain.request().newBuilder();
    getAuthParams().forEach(requestBuilder::addHeader);
    return chain.proceed(requestBuilder.build());
  }

  private Map<String, String> getAuthParams() {
    return Optional.ofNullable(authToken)
        .map(token -> Map.of(HEADER_AUTHORIZATION, BEARER_PREFIX + token))
        .orElse(Collections.emptyMap());
  }

  public void shutdown() {
    apolloClient.disableSubscriptions();
    waitForConnectionClosed();
  }

  @SneakyThrows
  private void waitForConnectionClosed() {
    // Found no other way to be sure that the websocket connections are really closed
    Thread.sleep(1000);
  }

}

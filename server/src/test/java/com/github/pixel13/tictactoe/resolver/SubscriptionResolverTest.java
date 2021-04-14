package com.github.pixel13.tictactoe.resolver;

import static org.mockito.Mockito.when;

import com.github.pixel13.tictactoe.domain.Game;
import com.github.pixel13.tictactoe.domain.Player;
import com.github.pixel13.tictactoe.security.AuthManager;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;
import reactor.test.StepVerifier;
import reactor.util.concurrent.Queues;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionResolver")
class SubscriptionResolverTest {


  private static final String FIRST_PLAYER_NAME = "firstPlayer";
  private static final String SECOND_PLAYER_NAME = "secondPlayer";
  private static final Player FIRST_PLAYER = new Player(FIRST_PLAYER_NAME);
  private static final Player SECOND_PLAYER = new Player(SECOND_PLAYER_NAME);
  private static final long NO_EVENTS_WAIT_MILLIS = 500L;
  private static final long MAX_TIMEOUT = 2L;

  @Mock
  private AuthManager mockAuthManager;

  @InjectMocks
  private SubscriptionResolver resolver;

  private final Many<Game> gameSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
  private final Flux<Game> gameFlux = gameSink.asFlux();

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(resolver, "gameEvents", gameFlux);
  }

  @Test
  @DisplayName("sends no data on opponentArrived subscription if the current player is not on move on the given game")
  void opponentArrivedNotOnMove() {
    Game game = new Game(SECOND_PLAYER);
    when(mockAuthManager.getCurrentPlayer()).thenReturn(FIRST_PLAYER);

    Publisher<String> result = resolver.opponentArrived();

    StepVerifier.create(result)
        .expectSubscription()
        .then(() -> gameSink.tryEmitNext(game))
        .expectNoEvent(Duration.ofMillis(NO_EVENTS_WAIT_MILLIS))
        .thenCancel()
        .verify(Duration.ofSeconds(MAX_TIMEOUT));
  }

  @Test
  @DisplayName("sends no data on opponentArrived subscription if the game is already started")
  void opponentArrivedAlreadyStarted() {
    Game game = new Game(FIRST_PLAYER);
    game.getBoard().put(1, 1);
    when(mockAuthManager.getCurrentPlayer()).thenReturn(FIRST_PLAYER);

    Publisher<String> result = resolver.opponentArrived();

    StepVerifier.create(result)
        .expectSubscription()
        .then(() -> gameSink.tryEmitNext(game))
        .expectNoEvent(Duration.ofMillis(NO_EVENTS_WAIT_MILLIS))
        .thenCancel()
        .verify(Duration.ofSeconds(MAX_TIMEOUT));
  }

  @Test
  @DisplayName("sends the second player name on opponentArrived subscription if the second player arrived and the game is not yet started")
  void opponentArrivedSendData() {
    Game game = new Game(FIRST_PLAYER);
    game.setSecondPlayer(SECOND_PLAYER);
    when(mockAuthManager.getCurrentPlayer()).thenReturn(FIRST_PLAYER);

    Publisher<String> result = resolver.opponentArrived();

    StepVerifier.create(result)
        .expectSubscription()
        .then(() -> gameSink.tryEmitNext(game))
        .expectNext(SECOND_PLAYER_NAME)
        .thenCancel()
        .verify(Duration.ofSeconds(MAX_TIMEOUT));
  }

  @Test
  @DisplayName("sends no data on opponentMove subscription if the current player is not on move on the given game")
  void opponentMoveNotOnMove() {
    Game game = new Game(FIRST_PLAYER);
    game.setSecondPlayer(SECOND_PLAYER);
    when(mockAuthManager.getCurrentPlayer()).thenReturn(SECOND_PLAYER);

    Publisher<Game> result = resolver.opponentMove();

    StepVerifier.create(result)
        .expectSubscription()
        .then(() -> gameSink.tryEmitNext(game))
        .expectNoEvent(Duration.ofMillis(NO_EVENTS_WAIT_MILLIS))
        .thenCancel()
        .verify(Duration.ofSeconds(MAX_TIMEOUT));
  }

  @Test
  @DisplayName("sends no data on opponentMove subscription if no move has been made yet")
  void opponentMoveNoMoveMade() {
    Game game = new Game(FIRST_PLAYER);
    game.setSecondPlayer(SECOND_PLAYER);
    when(mockAuthManager.getCurrentPlayer()).thenReturn(FIRST_PLAYER);

    Publisher<Game> result = resolver.opponentMove();

    StepVerifier.create(result)
        .expectSubscription()
        .then(() -> gameSink.tryEmitNext(game))
        .expectNoEvent(Duration.ofMillis(NO_EVENTS_WAIT_MILLIS))
        .thenCancel()
        .verify(Duration.ofSeconds(MAX_TIMEOUT));
  }

  @Test
  @DisplayName("sends game data on opponentMove subscription if current player is on move and at least one move has been made")
  void opponentMoveSendsData() {
    Game game = new Game(FIRST_PLAYER);
    game.setSecondPlayer(SECOND_PLAYER);
    game.getBoard().put(1, 1);
    when(mockAuthManager.getCurrentPlayer()).thenReturn(FIRST_PLAYER);

    Publisher<Game> result = resolver.opponentMove();

    StepVerifier.create(result)
        .expectSubscription()
        .then(() -> gameSink.tryEmitNext(game))
        .expectNext(game)
        .thenCancel()
        .verify(Duration.ofSeconds(MAX_TIMEOUT));
  }
}
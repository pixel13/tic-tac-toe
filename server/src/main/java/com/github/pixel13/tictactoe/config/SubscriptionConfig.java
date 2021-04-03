package com.github.pixel13.tictactoe.config;

import com.github.pixel13.tictactoe.domain.Game;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;
import reactor.util.concurrent.Queues;

@Configuration
public class SubscriptionConfig {

  @Bean
  public Many<Game> gameSink() {
    return Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
  }

  @Bean
  public Flux<Game> gameFlux(Many<Game> gameSink) {
    return gameSink.asFlux();
  }

}

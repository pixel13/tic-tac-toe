package com.github.pixel13.tictactoe.domain;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Player {

  @EqualsAndHashCode.Include
  String token = UUID.randomUUID().toString();

  String name;

  public Player(String name) {
    this.name = name;
  }
}

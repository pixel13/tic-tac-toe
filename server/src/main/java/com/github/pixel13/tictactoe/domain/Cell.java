package com.github.pixel13.tictactoe.domain;

import lombok.Value;

@Value
public class Cell {

  int row;
  int column;
  String value;

}

package com.github.pixel13.tictactoe.domain;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Board {

  private static final boolean STARTING_VALUE = true;

  private final Boolean[][] grid;
  private boolean nextValue = STARTING_VALUE;

  public Board(int size) {
    this.grid = new Boolean[size][size];
  }

  public boolean isEmpty(int x, int y) {
    return grid[x][y] == null;
  }

  public void put(int x, int y) {
    grid[x][y] = nextValue;
    nextValue = !nextValue;
  }

  public Boolean get(int x, int y) {
    return grid[x][y];
  }

  public List<Cell> toCells(String firstPlayerSign, String secondPlayerSign) {
    return IntStream.range(0, grid.length)
        .boxed()
        .flatMap(x -> IntStream.range(0, grid[x].length).mapToObj(y -> new Cell(x, y, valueToString(grid[x][y], firstPlayerSign, secondPlayerSign))))
        .collect(Collectors.toList());
  }

  private String valueToString(Boolean value, String firstPlayerSign, String secondPlayerSign) {
    if (value == null) {
      return null;
    }

    return value.equals(STARTING_VALUE) ? firstPlayerSign : secondPlayerSign;
  }
}

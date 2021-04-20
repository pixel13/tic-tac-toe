package com.github.pixel13.tictactoe.client;

import com.apollographql.apollo.ApolloSubscriptionCall;
import com.github.pixel13.tictactoe.client.mutation.MoveMutation;
import com.github.pixel13.tictactoe.client.mutation.StartGameMutation;
import com.github.pixel13.tictactoe.client.query.GameStatusQuery;
import com.github.pixel13.tictactoe.client.query.GameStatusQuery.Data;
import com.github.pixel13.tictactoe.client.query.OpponentMoveSubscription;
import com.github.pixel13.tictactoe.client.subscription.OpponentArrivedSubscription;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Game {

  private final Scanner scanner;
  private final GraphQLClient client;
  private boolean onMove;
  private final Map<Integer, Map<Integer, String>> board;
  private ApolloSubscriptionCall<?> subscription;

  public Game(Properties properties) {
    this.scanner = new Scanner(System.in);
    this.board = newBoard();

    String serverEndpoint = properties.getProperty("graphql.server.endpoint");
    String subscriptionEndpoint = properties.getProperty("graphql.subscriptions.endpoint");
    this.client = new GraphQLClient(serverEndpoint, subscriptionEndpoint);
  }

  public void startNewGame() {
    initializeGame().thenRun(this::makeFirstMove);
  }

  private CompletableFuture<Void> initializeGame() {
    String playerName = askForPlayerName();
    return client.mutate(new StartGameMutation(playerName))
        .thenCompose(this::setAuthTokenAndGetStatus)
        .thenCompose(this::waitForTheOpponent)
        .thenAccept(this::gameLoop);
  }

  private void makeFirstMove() {
    if (onMove) {
      makeAMove();
    } else {
      print("\nWaiting for the other player to make a move...");
    }
  }

  private CompletableFuture<Data> setAuthTokenAndGetStatus(StartGameMutation.Data data) {
    client.setAuthToken(data.getStartGame().getToken());
    return client.query(new GameStatusQuery());
  }

  private CompletableFuture<OpponentArrivedSubscription.Data> waitForTheOpponent(Data data) {
    updateBoard(data.getGameStatus().getBoard().stream().map(Cell::from));
    if (data.getGameStatus().isReady()) {
      onMove = false;
      return CompletableFuture.completedFuture(new OpponentArrivedSubscription.Data(data.getGameStatus().getOnMove()));
    } else {
      onMove = true;
      print("\nWaiting for an opponent...");
      return client.subscribeForOneValue(new OpponentArrivedSubscription());
    }
  }

  private void gameLoop(OpponentArrivedSubscription.Data data) {
    if (onMove) {
      print("\nAn opponent arrived: " + data.getOpponentArrived());
    } else {
      print("\nYou are playing against: " + data.getOpponentArrived());
    }

    printBoard();

    subscription = client.subscribe(new OpponentMoveSubscription(), this::showOpponentMoveAndTakeTurn);
  }

  private void showOpponentMoveAndTakeTurn(OpponentMoveSubscription.Data data) {
    updateBoard(data.getOpponentMove().getBoard().stream().map(Cell::from));
    print("\nThe opponent moved");
    printBoard();
    if (data.getOpponentMove().isOver()) {
      data.getOpponentMove().getWinner()
          .ifPresentOrElse(
              winner -> print("\n" + winner + " wins :("),
              () -> print("\n The game ends in a draw")
          );
      endGame();
    }
    makeAMove();
  }

  private void makeAMove() {
    int move = 0;
    while (move < 1 || move > 9) {
      ask("\nPlease make a move (1-9): ");
      try {
        move = scanner.nextInt();
      } catch (NoSuchElementException e) {
        scanner.next();
        move = 0;
      }
    }
    int row = Math.floorDiv(move - 1, 3);
    int column = Math.floorMod(move - 1, 3);

    client.mutate(new MoveMutation(row, column))
        .whenComplete((data, ex) -> {
          if (ex != null) {
            print("This is not a valid move, try again.");
            makeAMove();
            return;
          }
          updateBoard(data.getMove().getBoard().stream().map(Cell::from));
          printBoard();

          if (data.getMove().isOver()) {
            data.getMove().getWinner()
                .ifPresentOrElse(
                    winner -> print("\nYou win :)"),
                    () -> print("\n The game ends in a draw")
                );
            endGame();
          }

          print("\nWaiting for the other player to make a move...");
          onMove = !onMove;
        });
  }

  private Map<Integer, Map<Integer, String>> newBoard() {
    Map<Integer, Map<Integer, String>> boardMap = new TreeMap<>();
    boardMap.put(0, new TreeMap<>());
    boardMap.put(1, new TreeMap<>());
    boardMap.put(2, new TreeMap<>());

    return boardMap;
  }

  private void updateBoard(Stream<Cell> cells) {
    cells.forEach(cell -> {
      int row = cell.getRow();
      int column = cell.getColumn();
      String value = Optional.ofNullable(cell.getValue()).orElse(" ");
      board.get(row).put(column, value);
    });
  }

  private void printBoard() {
    String spacer = "   |   |   \n";
    String boardGrid = board.values().stream()
        .map(line -> " " + String.join(" │ ", line.values()) + " ")
        .collect(Collectors.joining("\n───┼───┼───\n"));
    print("\n" + spacer + boardGrid + "\n" + spacer);
  }

  private String askForPlayerName() {
    ask("\nEnter your name: ");
    return scanner.next();
  }

  private void ask(String string) {
    System.out.print(string);
  }

  private void print(String string) {
    System.out.println(string);
  }

  private void endGame() {
    subscription.cancel();
    client.shutdown();
    System.exit(0);
  }
}

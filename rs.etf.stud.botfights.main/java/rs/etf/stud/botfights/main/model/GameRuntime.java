package rs.etf.stud.botfights.main.model;

import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSpinner;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import rs.etf.stud.botfights.core.*;
import rs.etf.stud.botfights.main.App;
import rs.etf.stud.botfights.main.controllers.MainController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;

public class GameRuntime implements ViewControl, GameActions, ViewContext {
    private MainController mainController;
    private Game game;
    private List<Player> players;
    private GameThread gameThread;
    private Scene scene;
    private StackPane root;
    private Stage loadingStage;
    private Stage gameStage;
    private boolean gameWindowOpen;
    private int pause;
    private boolean sticky;
    private Semaphore viewSync;


    public GameRuntime(Game game, List<Player> players, MainController mainController) {
        this.game = game;
        this.players = players;
        this.mainController = mainController;
        pause = 0;
        sticky = false;
        viewSync = new Semaphore(0);
    }

    public void launch() {
        gameThread = new GameThread();
        gameThread.start();
        showLoadingWindow();
    }

    private void showLoadingWindow() {
        Text loadingText = new Text("Loading");
        loadingText.setStyle("-fx-background-color: transparent");
        loadingText.setFont(Font.font("Roboto-Bold", 20));
        List<JFXSpinner> spinners = new ArrayList<>(10);
        for (int i = 70; i <= 100; i += 10) {
            JFXSpinner spinner = new JFXSpinner();
            spinner.setStyle("-fx-background-color: transparent");
            spinner.setRadius(i);
            spinners.add(spinner);
        }

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: white");
        root.getChildren().addAll(spinners);
        root.getChildren().add(loadingText);

        loadingStage = new Stage();
        loadingStage.setAlwaysOnTop(true);
        loadingStage.setTitle(game.getName());
        loadingStage.initStyle(StageStyle.TRANSPARENT);


        Scene scene = new Scene(root, 210, 210);
        scene.setFill(Color.TRANSPARENT);
        loadingStage.setScene(scene);

        try {
            loadingStage.showAndWait();
        } catch (IllegalStateException ex) {
            //;
        }

    }

    public synchronized void setGameWindowOpen(boolean windowClosed) {
        this.gameWindowOpen = windowClosed;
    }

    @Override
    public void initGame() {
        game.initGame();
    }

    @Override
    public void onPlayerMove(Move move, Player player) {
        try {
            LoggerUtil.getLogger().log("Player " + player.getName() + " plays move: " + move.getCommand());
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        game.onPlayerMove(move, player);
    }

    @Override
    public GameOutcome getGameOutcome() {
        return game.getGameOutcome();
    }

    @Override
    public void cleanupGame() {
        game.cleanupGame();
    }

    @Override
    public boolean endCondition() {
        return game.endCondition();
    }


    private void pace() {
        try {
            viewSync.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (pause != 0) {
            try {
                Thread.sleep(pause);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!sticky) {
            pause = 0;
        }
    }

    @Override
    public void onGameStart() {
        synchronized (this){
            if (!gameWindowOpen) return;
        }
        Platform.runLater(() -> {
            game.onGameStart(this);
            viewSync.release();
        });
        pace();
    }


    @Override
    public void onTurnStart() {
        synchronized (this){
            if (!gameWindowOpen) return;
        }
        Platform.runLater(() -> {
            game.onTurnStart(this);
            viewSync.release();
        });
        pace();
    }

    @Override
    public void beforePlayerTurn(Player player) {
        synchronized (this){
            if (!gameWindowOpen) return;
        }
        Platform.runLater(() -> {
            game.beforePlayerTurn(this, player);
            viewSync.release();
        });
        pace();

    }

    @Override
    public void afterPlayerTurn(Player player, Move move) {
        synchronized (this){
            if (!gameWindowOpen) return;
        }
        Platform.runLater(() -> {
            game.afterPlayerTurn(this, player, move);
            viewSync.release();
        });
        pace();
    }

    @Override
    public void customManipulation(SceneManipulation SceneManipulation) {
        synchronized (this){
            if (!gameWindowOpen) return;
        }
        Platform.runLater(() -> {
            SceneManipulation.manipulate(scene);
            viewSync.release();
        });
        pace();
    }

    @Override
    public void onTurnEnd() {
        synchronized (this){
            if (!gameWindowOpen) return;
        }
        Platform.runLater(() -> {
            game.onTurnEnd(this);
            viewSync.release();
        });
        pace();
    }

    @Override
    public void onGameEnd() {
        synchronized (this){
            if (!gameWindowOpen) return;
        }
        Platform.runLater(() -> {
            game.onGameEnd(this);
            viewSync.release();
        });
        pace();
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public void pause(int ms, boolean sticky) {
        this.pause = ms;
        this.sticky = sticky;
    }

    private class GameThread extends Thread {
        Semaphore gameThreadSync = new Semaphore(0);
        private boolean windowClosed = false;

        @Override
        public void run() {
            LoggerUtil.getLogger().log("Game thread started!");
            try {
                LoggerUtil.getLogger().log("Loading runners...");
                Collection<Runner> runners = loadRunners();
                try {
                    LoggerUtil.getLogger().log("Starting runners...");
                    runners.forEach(Runner::start);

                    LoggerUtil.getLogger().log("Initializing game...");
                    game.setPlayers(players);
                    game.initGame();
                    GameFlow gameFlow = game.getFlow();
                    PlayerManager playerManager = new PlayerManager(players);

                    LoggerUtil.getLogger().log("Initializing game scene...");
                    JFXSnackbar snackbar = initGameScene();

                    LoggerUtil.getLogger().log("Showing game window...");
                    Platform.runLater(() -> showGameWindow(snackbar));

                    gameThreadSync.acquire();
                    LoggerUtil.getLogger().log("Game starting...");

                    gameFlow.init(GameRuntime.this, GameRuntime.this, playerManager, null);
                    gameFlow.flow(GameRuntime.this, GameRuntime.this, playerManager, null);
                    GameOutcome outcome = gameFlow.end(GameRuntime.this, GameRuntime.this, playerManager, null);
                    LoggerUtil.getLogger().log("Game ended with the outcome: " + outcome.getDescription());

                    if (GameRuntime.this.gameWindowOpen) {
                        Platform.runLater(() -> {
                            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("Looks like we are done here. You can close the game window.\nCheck consoles for more output.",
                                    null, 7000, false, event -> {
                            }));
                        });
                    }
                } finally {
                    LoggerUtil.getLogger().log("Stopping runners...");
                    runners.forEach(Runner::stop);
                }
                LoggerUtil.getLogger().log("Waiting for game window to close.");
                gameThreadSync.acquire();
            } catch (InterruptedException e) {
                //
            } catch (Exception ex) {
                ex.printStackTrace();
                JFXDialog jfxDialog = null;
                try {
                    jfxDialog = FXMLLoader.load(getClass().getResource("/rs/etf/stud/botfights/main/view/fxml/whopsdialog.fxml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final JFXDialog dialog = jfxDialog;
                Platform.runLater(() -> {
                    if (loadingStage != null) {
                        loadingStage.close();
                    }
                    if (gameStage != null) {
                        gameStage.close();
                    }
                    if (dialog != null) {
                        dialog.show(App.getMainController().root);
                    }
                });
            } finally {
                App.getMainController().setRunning(false);
                Platform.runLater(() -> {
                    App.getMainController().makeMutable();

                    if (loadingStage != null) {
                        loadingStage.close();
                    }

                    if (gameStage != null) {
                        gameStage.close();
                    }
                });
                LoggerUtil.getLogger().log("Game thread ended!");
            }
        }

        private void showGameWindow(JFXSnackbar snackbar) {
            if (loadingStage != null) {
                loadingStage.close();
            }
            gameStage = new Stage();
            gameStage.setTitle(game.getName());
            JFXDecorator decorator = new JFXDecorator(gameStage, scene.getRoot());
            decorator.setCustomMaximize(true);
            decorator.getStylesheets().add(getClass().getResource("/rs/etf/stud/botfights/main/view/css/decorator.css").toExternalForm());
            scene.setRoot(decorator);
            gameStage.setScene(scene);
            gameStage.setOnHiding(event -> {
                GameRuntime.this.setGameWindowOpen(false);
                GameThread.this.interrupt();
                LoggerUtil.getLogger().log("Window closing...");
                gameThreadSync.release(2);
                viewSync.release(1);
            });

            gameStage.show();
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("Looks like we are ready. You can start the game by clicking anywhere on the screen.",
                    null, 5000, false, event -> {
            }));
            GameRuntime.this.setGameWindowOpen(true);
        }

        private JFXSnackbar initGameScene() {
            root = new StackPane();
            JFXSnackbar snackbar = new JFXSnackbar(root);
            snackbar.setStyle("-fx-background-color: #323232; -fx-text-fill: WHITE;");
            EventHandler<MouseEvent> eventHandler = new EventHandler<>() {
                @Override
                public void handle(MouseEvent event) {
                    snackbar.close();
                    gameThreadSync.release();
                    root.removeEventFilter(MouseEvent.MOUSE_CLICKED, this);
                }
            };
            root.addEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);

            Scene tempGameScene = game.initScene();

            Parent content = tempGameScene.getRoot();
            tempGameScene.setRoot(new Pane());

            root.getChildren().add(content);
            root.getStylesheets().add(getClass().getResource("/rs/etf/stud/botfights/main/view/css/snackbar.css").toExternalForm());
            scene = new Scene(root, tempGameScene.getWidth(), tempGameScene.getHeight());
            return snackbar;
        }

        private Collection<Runner> loadRunners() {
            Collection<Runner> runners = new ArrayList<>(10);
            for (Player player : players) {
                Runner r = game.getRunner(player.getAlgFilePath());
                if (r == null) {
                    LoggerUtil.getLogger().log("Game doesn't recognize file: " + player.getAlgFilePath() + " as a runnable file. Check file name/extension.");
                    throw new RuntimeException("Runner null.");
                }
                r.subscribePlayer(player);
                runners.add(r);
            }
            return runners;
        }
    }
}

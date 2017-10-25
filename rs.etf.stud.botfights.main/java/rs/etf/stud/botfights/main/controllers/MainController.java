package rs.etf.stud.botfights.main.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSpinner;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;
import rs.etf.stud.botfights.core.Game;
import rs.etf.stud.botfights.core.LoggerUtil;
import rs.etf.stud.botfights.core.Player;
import rs.etf.stud.botfights.core.Template;
import rs.etf.stud.botfights.main.App;
import rs.etf.stud.botfights.main.components.GameLabel;
import rs.etf.stud.botfights.main.components.PlayerVBox;
import rs.etf.stud.botfights.main.model.GameRuntime;
import rs.etf.stud.botfights.main.model.Model;
import rs.etf.stud.botfights.main.utility.Console;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {
    @FXML
    public StackPane webViewParent;

    @FXML
    public StackPane root;

    @FXML
    JFXListView<GameLabel> gamesListView;

    @FXML
    WebView webView;

    @FXML
    JFXComboBox<Template> templateComboBox;

    @FXML
    JFXButton downloadTemplateBtn;

    @FXML
    VBox playerVBoxContainer;
    @FXML
    JFXButton addPlayerBtn;
    @FXML
    JFXButton removePlayerBtn;

    @FXML
    JFXButton launchBtn;

    @FXML
    TextArea textArea;

    private JFXSpinner webViewLoadProgress;

    private List<PlayerVBox> playerVBoxList;

    private boolean running = false;

    public StackPane getRoot() {
        return root;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        App.setMainController(this);

        gamesListView.setPlaceholder(new Label("No Games Found :("));
        LoggerUtil.setInstance(new Console(textArea, System.out));

        webViewLoadProgress = new JFXSpinner();
        webViewLoadProgress.setRadius(50);
        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.equals(Worker.State.SUCCEEDED)){
                NodeList nodeList = webView.getEngine().getDocument().getElementsByTagName("a");
                for (int i = 0; i < nodeList.getLength(); i++)
                {
                    Node node= nodeList.item(i);
                    EventTarget eventTarget = (EventTarget) node;
                    EventListener linkRedirect = new EventListener() {
                        @Override
                        public void handleEvent(Event evt)
                        {
                            EventTarget target = evt.getCurrentTarget();
                            HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
                            String href = anchorElement.getHref();
                            Desktop d = Desktop.getDesktop();
                            try {
                                d.browse(URI.create(href));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            evt.preventDefault();
                        }
                    };
                    eventTarget.addEventListener("click", linkRedirect, false);
                }
                webViewParent.getChildren().remove(webViewLoadProgress);
                LoggerUtil.getLogger().log("Description loaded");
            }
        });

        webView.getEngine().setCreatePopupHandler(param -> {
            return null;
        });

        webView.getEngine().load(getClass().getResource("/rs/etf/stud/botfights/main/view/html/about.html").toExternalForm());

        gamesListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((gameLabel, oldValue, newValue) -> {
                    if (newValue != null) {
                        Model.getInstance().selectGame(newValue.getGame());
                        loadGame();
                    }
                });

        templateComboBox.setCellFactory(param ->
                new ListCell<Template>() {
                    @Override
                    protected void updateItem(Template item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : item.getLanguage());
                    }
                }
        );
        templateComboBox.setLabelFloat(true);
        templateComboBox.setConverter(new StringConverter<Template>() {
            @Override
            public String toString(Template object) {
                return object.getLanguage();
            }

            @Override
            public Template fromString(String string) {
                return null;
            }
        });

        downloadTemplateBtn.setOnAction(this::downloadTemplate);

        playerVBoxList = new ArrayList<>();
        addPlayerBtn.setOnMouseClicked(this::addPlayerVBox);
        removePlayerBtn.setOnMouseClicked(this::removePlayerVBox);

        launchBtn.setOnAction(this::launchGame);
    }

    private void loadGame() {
        Game game = Model.getInstance().getCurrentGame();
        LoggerUtil.getLogger().log("Loading info:"+game.getName()+"...");

        if (game != null) {
            loadDescription(game);
            loadTemplates(game);
            loadPlayerManager(game);
            launchBtn.setDisable(true);
        }

    }

    private void loadDescription(Game game) {
        webViewParent.getChildren().add(webViewLoadProgress);
        String gameDescription = game.getDescription();
        if (gameDescription.matches("^.*\\.html$")) {
            webView.getEngine().load(gameDescription);
        } else {
            webView.getEngine().loadContent(gameDescription);
        }
    }

    private void loadTemplates(Game game) {
        templateComboBox.getItems().clear();
        templateComboBox.getItems().addAll(game.getTemplates());
    }
    
    private void loadPlayerManager(Game game){
        playerVBoxList.clear();
        playerVBoxContainer.getChildren().clear();
        addPlayerBtn.setDisable(true);
        removePlayerBtn.setDisable(true);
        for (int i = 0; i < game.getMinPlayers(); i++) {
            addPlayerVBox(null);
        }
        checkPlayerManagerButtons();
    }

    private void removePlayerVBox(MouseEvent event) {
        if (playerVBoxList.size() > Model.getInstance().getCurrentGame().getMinPlayers()) {
            PlayerVBox vbox = playerVBoxList.remove(playerVBoxList.size() - 1);
            playerVBoxContainer.getChildren().remove(vbox);
            Platform.runLater(() -> {
                checkPlayerManagerButtons();
                checkLaunchButton();
            });
        }

    }

    private void addPlayerVBox(MouseEvent event) {
        if(playerVBoxList.size() < Model.getInstance().getCurrentGame().getMaxPlayers()){
            PlayerVBox playerVBox = new PlayerVBox();
            playerVBox.addAlgorithmListener((observable, oldValue, newValue) -> onFocusAlgorithm(playerVBox, newValue));
            playerVBoxList.add(playerVBox);
            playerVBoxContainer.getChildren().add(playerVBox);

            Platform.runLater(this::checkPlayerManagerButtons);
            launchBtn.setDisable(true);
        }
    }

    private void checkPlayerManagerButtons(){
        if(playerVBoxList.size() <= Model.getInstance().getCurrentGame().getMinPlayers()) {
            removePlayerBtn.setDisable(true);
            playerVBoxContainer.requestFocus();
        }else{
            removePlayerBtn.setDisable(false);
        }

        if(playerVBoxList.size() >= Model.getInstance().getCurrentGame().getMaxPlayers()){
            addPlayerBtn.setDisable(true);
            playerVBoxContainer.requestFocus();
        }else{
            addPlayerBtn.setDisable(false);
        }
    }

    private void launchGame(ActionEvent event) {
        boolean okToLaunch = true;
        //wierd behaviour
        playerVBoxList.forEach(playerVBox -> playerVBox.getPlayerName().validate());
        for (PlayerVBox playerVBox : playerVBoxList) {
            okToLaunch = okToLaunch && playerVBox.getPlayerAlgorithm().validate();
            okToLaunch = okToLaunch && playerVBox.getPlayerName().validate();
        }

        if(okToLaunch && !running){
            try {
                Game game = Model.getInstance().getCurrentGame().getClass().getDeclaredConstructor().newInstance();
                LoggerUtil.getLogger().log("Launching "+game.getName()+"...");
                running = true;
                makeImmutable();
                GameRuntime gameRuntime = new GameRuntime(game,
                        playerVBoxList
                                .stream()
                                .map(playerVBox -> new Player(playerVBox.getPlayerName().getText() ,playerVBox.getPlayerAlgorithm().getText(), game))
                                .collect(Collectors.toList()),this);
                gameRuntime.launch();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public void makeMutable() {
        playerVBoxContainer.setDisable(false);
        gamesListView.setDisable(false);
        launchBtn.setDisable(false);
        addPlayerBtn.setDisable(false);
        removePlayerBtn.setDisable(false);
    }

    public void makeImmutable() {
        playerVBoxContainer.setDisable(true);
        gamesListView.setDisable(true);
        launchBtn.setDisable(true);
        addPlayerBtn.setDisable(true);
        removePlayerBtn.setDisable(true);

    }

    public void updateGamesList(Collection<Game> games) {
        gamesListView.getItems().clear();
        gamesListView.getItems().addAll(games.stream()
                .map(game -> new GameLabel(game.getName(), game))
                .collect(Collectors.toList()));

    }

    private void checkLaunchButton() {
        boolean flag = true;
        if (playerVBoxList.isEmpty()) {
            launchBtn.setDisable(true);
            return;
        }
        for (PlayerVBox vbox : playerVBoxList) {
            if (vbox.getPlayerAlgorithm().getText().isEmpty()) {
                flag = false;
                break;
            }
        }
        if (flag) {
            launchBtn.setDisable(false);
        }
    }

    private void downloadTemplate(ActionEvent actionEvent){
        Template template = templateComboBox.getSelectionModel().getSelectedItem();
        if (template != null) {
            FileChooser fileChooser = initFileChooserForSaving(template);
            File fileToSave = fileChooser.showSaveDialog(App.getPrimaryStage());
            if(fileToSave == null) return;
            try (InputStream is = Model.getInstance().getCurrentGame().getClass().getResourceAsStream(template.getTemplateFileName())) {
                Files.copy(is, fileToSave.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onFocusAlgorithm(PlayerVBox playerVBox, Boolean focused) {
        if (focused) {
            playerVBoxContainer.requestFocus();
            FileChooser fileChooser = initFileChooserForOpenFile();
            File f = fileChooser.showOpenDialog(App.getPrimaryStage());
            if (f != null) {
                playerVBox.getPlayerAlgorithm().setText(f.getAbsolutePath());
                checkLaunchButton();
            }
        }
    }

    private FileChooser initFileChooserForSaving(Template template) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Template");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName(template.getTemplateFileName().substring(template.getTemplateFileName().lastIndexOf('/') + 1));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        return fileChooser;
    }

    private FileChooser initFileChooserForOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters()
                .addAll(Model.getInstance().getCurrentGame().getRecognizedFileExtensions()
                        .stream()
                        .map(fileExtension -> new FileChooser.ExtensionFilter(fileExtension.getFileType(), fileExtension.getFileExtension()))
                        .collect(Collectors.toList()));
        return fileChooser;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void kill(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

    public void about(ActionEvent actionEvent) {
        gamesListView.getSelectionModel().clearSelection();
        Model.getInstance().selectGame(null);
        playerVBoxContainer.getChildren().clear();
        playerVBoxList.clear();
        launchBtn.setDisable(true);
        addPlayerBtn.setDisable(true);
        removePlayerBtn.setDisable(true);
        webView.getEngine().load(getClass().getResource("/rs/etf/stud/botfights/main/view/html/about.html").toExternalForm());
    }

    public void clearConsole(ActionEvent actionEvent) {
        textArea.clear();
    }
}

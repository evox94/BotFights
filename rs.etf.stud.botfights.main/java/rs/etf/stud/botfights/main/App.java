package rs.etf.stud.botfights.main;

import com.jfoenix.controls.JFXDecorator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import rs.etf.stud.botfights.core.*;
import rs.etf.stud.botfights.main.controllers.MainController;
import rs.etf.stud.botfights.main.model.Model;

public class App extends Application{

    private static Stage primaryStage;
    public static Stage getPrimaryStage(){
        return primaryStage;
    }

    private static MainController mainController;

    public static MainController getMainController() {
        return mainController;
    }

    public static void setMainController(MainController mainController) {
        App.mainController = mainController;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadFonts();
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/rs/etf/stud/botfights/main/view/images/botFightsIcon.jpg")));
        primaryStage.setOnCloseRequest(event -> {
            try {
                Platform.exit();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Parent root = FXMLLoader.load(getClass().getResource("/rs/etf/stud/botfights/main/view/fxml/main.fxml"));

        primaryStage.setTitle("Botfights");
        primaryStage.setScene(new Scene(root, 1280 , 800));

        getMainController().updateGamesList(Model.getInstance().getGames());
        App.primaryStage = primaryStage;

        primaryStage.show();
    }

    private void loadFonts() {
        Font robotoMedium = Font.loadFont(getClass().getResource("/rs/etf/stud/botfights/main/view/fonts/Roboto-Regular.ttf").toExternalForm(),16);
        Font robotoBold = Font.loadFont(getClass().getResource("/rs/etf/stud/botfights/main/view/fonts/Roboto-Bold.ttf").toExternalForm(),20);
        Font indieFlower = Font.loadFont(getClass().getResource("/rs/etf/stud/botfights/main/view/fonts/IndieFlower.ttf").toExternalForm(),20);
    }
}

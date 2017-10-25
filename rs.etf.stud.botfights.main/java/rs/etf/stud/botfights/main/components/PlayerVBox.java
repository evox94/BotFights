package rs.etf.stud.botfights.main.components;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.VBox;

public class PlayerVBox extends VBox {

    private JFXTextField playerName;
    private JFXTextField playerAlgorithm;

    public PlayerVBox() {
        super();
        setSpacing(10);
        this.playerName = new JFXTextField();
        this.playerName.setPromptText("Player Name:");
        this.playerName.setLabelFloat(true);
        RequiredFieldValidator nameValidator = new RequiredFieldValidator();
        nameValidator.setMessage("Required");
        playerName.getValidators().add(nameValidator);
        playerName.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue)playerName.validate();
        });
        playerName.textProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue == null || oldValue.isEmpty())playerName.validate();
        });

        RequiredFieldValidator algValidator = new RequiredFieldValidator();
        algValidator.setMessage("Required");
        this.playerAlgorithm = new JFXTextField();
        this.playerAlgorithm.setPromptText("Select algorithm file:");
        this.playerAlgorithm.getValidators().add(algValidator);
        this.getChildren().addAll(this.playerName, this.playerAlgorithm);
    }

    public void addAlgorithmListener(ChangeListener<? super Boolean> callback){
        this.playerAlgorithm.focusedProperty().addListener(callback);
    }

    public JFXTextField getPlayerAlgorithm() {
        return playerAlgorithm;
    }

    public JFXTextField getPlayerName() {
        return playerName;
    }
}

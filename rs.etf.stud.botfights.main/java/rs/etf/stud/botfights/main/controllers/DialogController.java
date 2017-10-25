package rs.etf.stud.botfights.main.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class DialogController implements Initializable{

    public JFXButton acceptButton;
    public JFXDialog dialog;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
        acceptButton.setOnAction(event -> {
            dialog.close();
        });
    }
}

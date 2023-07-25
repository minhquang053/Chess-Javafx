package com.yelaco.chessgui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ChessController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
package com.yelaco.chessgui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private BorderPane root;
    @FXML
    private VBox boxbtn;
    @FXML
    private VBox gamebtn;
    @FXML
    private Button btnReset;
    @FXML
    private Button btnHint;
    @FXML
    private Button btnResign;
    @FXML
    private Button btnOffline;
    @FXML
    private Button btnOnline;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String[] tmp = url.toString().split("/");
        tmp[tmp.length-1] = "";
        String rootPath = String.join("/", tmp);

        btnReset.setGraphic(new ImageView(new Image(rootPath + "img/replay.png")));
        ((ImageView) btnReset.getGraphic()).setFitHeight(60.0);
        ((ImageView) btnReset.getGraphic()).setFitWidth(60.0);
        ((ImageView) btnReset.getGraphic()).setSmooth(true);
        btnReset.setStyle("-fx-padding-insets: 0; -fx-border-insets: 0; -fx-background-insets: 0; -fx-background-color: transparent");

        btnHint.setGraphic(new ImageView(new Image(rootPath + "img/hint.png")));
        ((ImageView) btnHint.getGraphic()).setFitHeight(60.0);
        ((ImageView) btnHint.getGraphic()).setFitWidth(60.0);
        ((ImageView) btnHint.getGraphic()).setSmooth(true);
        btnHint.setStyle("-fx-padding-insets: 0; -fx-border-insets: 0; -fx-background-insets: 0; -fx-background-color: transparent");

        btnResign.setGraphic(new ImageView(new Image(rootPath + "img/resign.png")));
        ((ImageView) btnResign.getGraphic()).setFitHeight(60.0);
        ((ImageView) btnResign.getGraphic()).setFitWidth(60.0);
        ((ImageView) btnResign.getGraphic()).setSmooth(true);
        btnResign.setStyle("-fx-padding-insets: 0; -fx-border-insets: 0; -fx-background-insets: 0; -fx-background-color: transparent");

        root.setStyle("-fx-background-image: url(\"" + rootPath + "img/intro-background.jpg\")");
    }

    public void makeOfflineMatch() {
        loadScreen("play-view.fxml");
    }
    private void loadScreen(String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            switch (resource) {
                case "play-view.fxml" -> {
                    root.setCenter(loader.load());
                    btnOffline.setDisable(true);
                    gamebtn.setVisible(true);
                }
                case "online-view.fxml" -> {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            root.setCenter(null);
        }
    }
}

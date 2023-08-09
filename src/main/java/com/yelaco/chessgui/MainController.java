package com.yelaco.chessgui;

import com.yelaco.common.ComputerPlayer;
import com.yelaco.common.GameStatus;
import com.yelaco.common.HumanPlayer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
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
    private Button btnRewind;
    @FXML
    private Button btnResign;
    @FXML
    private Button btnOffline;
    @FXML
    private Button btnOnline;

    private PlayController pc;

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

        btnRewind.setGraphic(new ImageView(new Image(rootPath + "img/rewind.png")));
        ((ImageView) btnRewind.getGraphic()).setFitHeight(60.0);
        ((ImageView) btnRewind.getGraphic()).setFitWidth(60.0);
        ((ImageView) btnRewind.getGraphic()).setSmooth(true);
        btnRewind.setStyle("-fx-padding-insets: 0; -fx-border-insets: 0; -fx-background-insets: 0; -fx-background-color: transparent");

        btnResign.setGraphic(new ImageView(new Image(rootPath + "img/resign.png")));
        ((ImageView) btnResign.getGraphic()).setFitHeight(60.0);
        ((ImageView) btnResign.getGraphic()).setFitWidth(60.0);
        ((ImageView) btnResign.getGraphic()).setSmooth(true);
        btnResign.setStyle("-fx-padding-insets: 0; -fx-border-insets: 0; -fx-background-insets: 0; -fx-background-color: transparent");

        root.setStyle("-fx-background-image: url(\"" + rootPath + "img/intro-background.jpg\")");
    }

    public void makeOfflineMatch() {
        Opponent[] opponents = {Opponent.COMPUTER, Opponent.PLAYER2};
        ChoiceDialog<Opponent> mode = new ChoiceDialog<>(Opponent.COMPUTER, opponents);
        mode.setTitle("Offline mode");
        mode.setContentText("Choose your opponent");
        mode.setHeaderText("Welcome chess player!");
        mode.setGraphic(null);
        mode.showAndWait();
        if (mode.getResult() == null) {
            return;
        }
        var opponent = mode.getSelectedItem();

        loadScreen("play-view.fxml");
        var color = pc.game.getPlayers()[1].isWhiteSide();
        switch (opponent) {
            case COMPUTER -> {;
                pc.setOpponent(new ComputerPlayer(color, 300));
            }
            case PLAYER2 -> {
                pc.setOpponent(new HumanPlayer(color));
                btnRewind.setDisable(true);
            }
        }
    }

    public void reverseMove() {
        if (pc.currentPlayer instanceof ComputerPlayer) {
            return;
        }
        pc.reverseMove();
        pc.reverseMove();
    }

    public void resignMatch() {
        if (pc.game.isOver()) {
            return;
        }
        if (pc.currentPlayer.isWhiteSide) {
            pc.game.setStatus(GameStatus.BLACK_WIN);
        } else {
            pc.game.setStatus(GameStatus.WHITE_WIN);
        }
        pc.displayGameOver();
    }

    private void loadScreen(String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            switch (resource) {
                case "play-view.fxml" -> {
                    root.setCenter(loader.load());
                    btnOffline.setDisable(true);
                    btnRewind.setDisable(false);
                    gamebtn.setVisible(true);
                    this.pc = loader.getController();
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

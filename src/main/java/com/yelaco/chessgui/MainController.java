package com.yelaco.chessgui;

import com.yelaco.common.ComputerPlayer;
import com.yelaco.common.GameStatus;
import com.yelaco.common.HumanPlayer;
import com.yelaco.common.Player;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    public boolean playAsWhite;
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

    public PlayController pc;
    public OfflineController oc;

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
        CheckBox optSide = new CheckBox("Play as white");
        optSide.setSelected(true);
        ChoiceDialog<Opponent> mode = new ChoiceDialog<>(Opponent.COMPUTER, opponents);
        mode.setGraphic(null);
        mode.getDialogPane().setExpandableContent(optSide);
        mode.getDialogPane().setExpanded(true);
        mode.setTitle("Offline mode");
        mode.setContentText("Choose your opponent");
        mode.setHeaderText("Welcome chess player!");
        mode.showAndWait();
        if (mode.getResult() == null) {
            return;
        }
        var opponent = mode.getSelectedItem();
        playAsWhite = optSide.isSelected();
        switch (opponent) {
            case COMPUTER -> {
                loadScreen("offline-view.fxml");
            }
            case PLAYER2 -> {
                if (playAsWhite) {
                    loadScreen("play-view.fxml");
                } else {
                    loadScreen("rev-play-view.fxml");
                }
                pc.initMatch(new HumanPlayer(playAsWhite), new HumanPlayer(!playAsWhite));
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
        if (pc.whiteList.getItems().isEmpty()) {
            return;
        }
        pc.whiteList.getItems().remove(pc.whiteList.getItems().size()-1);
        pc.blackList.getItems().remove(pc.blackList.getItems().size() - 1);
        pc.numList.getItems().remove(pc.numList.getItems().size() - 1);
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

    public void initMatch(Player p1, Player p2) {
        if (playAsWhite) {
            loadScreen("play-view.fxml");
        } else {
            loadScreen("rev-play-view.fxml");
        }
        Platform.runLater(() -> {
            pc.initMatch(p1, p2);
        });
    }

    public void loadScreen(String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            switch (resource) {
                case "offline-view.fxml" -> {
                    root.setCenter(loader.load());
                    this.oc = loader.getController();
                    oc.setMainController(this);
                }
                case "play-view.fxml", "rev-play-view.fxml" -> {
                    root.setCenter(loader.load());
                    this.pc = loader.getController();
                    if (oc != null) {
                        this.pc.setComputerElo(oc.compElo);
                        this.pc.setComputerName(oc.compName);
                    }
                    btnOffline.setDisable(true);
                    btnRewind.setDisable(false);
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

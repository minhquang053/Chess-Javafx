package com.yelaco.chessgui;

import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class PromoteController implements Initializable {
    private PlayController pc;
    public PromoteController(PlayController pc) {
        this.pc = pc;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void setPromoteChoice(MouseEvent event) {
        var imgView = (ImageView) event.getSource();
        pc.game.setPromote(new File(imgView.getImage().getUrl()).getName());
        pc.promotingPiece.setImage(imgView.getImage());
        pc.game.getCurrentTurn();
        pc.makeMoveSound(pc.game.getLastMovePlayed());
        pc.rootProm.setVisible(false);
        pc.showingProm.setCenter(null);
        pc.promotingPiece = null;
        if (pc.game.isOver()) {
            pc.displayGameOver();
        }

        // to stop the event from propagating to parent layers
        event.consume();
    }

    public void exitPromoteChoice(MouseEvent event) {
        pc.exitPromoteChoice(event);
    }
}

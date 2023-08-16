package com.yelaco.chessgui;

import com.yelaco.common.ComputerPlayer;
import com.yelaco.common.HumanPlayer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class OfflineController implements Initializable {
    private MainController mc;
    public String compElo;
    public String compName;
    private BorderPane chosenPane;
    @FXML
    private BorderPane level1;
    @FXML
    private BorderPane level2;
    @FXML
    private BorderPane level3;
    @FXML
    private BorderPane level4;
    @FXML
    private BorderPane level5;
    @FXML
    private BorderPane level6;
    @FXML
    private Label opname;
    @FXML
    private Label opelo;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    private String getElo(BorderPane pane) {
        if (pane == level1) {
            return "750";
        } else if (pane == level2) {
            return "1250";
        } else if (pane == level3) {
            return "1750";
        } else if (pane == level4) {
            return "2500";
        } else if (pane == level5) {
            return "3000";
        } else if (pane == level6) {
            return "3200";
        }
        return "0";
    }

    private String getName(BorderPane pane) {
        if (pane == level1) {
            return "Yuji Itadori";
        } else if (pane == level2) {
            return "Yuta Okkutsu";
        } else if (pane == level3) {
            return "Geto Suguru";
        } else if (pane == level4) {
            return "Fushiguro Toji";
        } else if (pane == level5) {
            return "Gojo Satoru";
        } else if (pane == level6) {
            return "Sukuna";
        }
        return "Null";
    }

    public void chooseLevel() {
        if (chosenPane == null) {
            return;
        }
        compElo = getElo(chosenPane);
        compName = getName(chosenPane);
        mc.initMatch(new HumanPlayer(mc.playAsWhite), new ComputerPlayer(!mc.playAsWhite, 300));
    }

    public void choosePane(MouseEvent event) {
        var curPane = (BorderPane) event.getSource();
        if (curPane != chosenPane) {
            hightlightPane(curPane);
            unhighlightPane(chosenPane);
            opname.setText(getName(curPane));
            opelo.setText(getElo(curPane));
            chosenPane = curPane;
        } else {
            unhighlightPane(curPane);
            opname.setText("");
            opelo.setText("");
            chosenPane = null;
        }
    }

    private void hightlightPane(BorderPane pane) {
        pane.setStyle("-fx-border-color: rgba(125,176,75,255); -fx-border-width: 6; -fx-border-radius: 10%");
    }

    private void unhighlightPane(BorderPane pane) {
        if (pane == null) {
            return;
        }
        pane.setStyle("");
    }

    public void setMainController(MainController mc) {
        this.mc = mc;
    }
}

module com.yelaco.chessgui {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.yelaco.chessgui to javafx.fxml;
    exports com.yelaco.chessgui;
}
module com.yelaco.chessgui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.yelaco.chessgui to javafx.fxml;
    exports com.yelaco.chessgui;
}
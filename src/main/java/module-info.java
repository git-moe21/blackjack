module com.example.blackjack.views {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.blackjack.views to javafx.fxml;
    exports com.example.blackjack.views;
    exports com.example.blackjack.controllers;
    opens com.example.blackjack.controllers to javafx.fxml;
}
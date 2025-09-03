package com.example.blackjack.controllers;

import javafx.fxml.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.*;
import java.io.*;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;

/**
 * Controller-Klasse zur Verwaltung der Login-Funktionalität der Anwendung.
 */
public class LoginController {
    private Stage stage;
    private DataOutputStream out;
    private DataInputStream in;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    /**
     * Konstruktor für den LoginController.
     * @param stage Die Hauptbühne der Anwendung.
     * @param out DataOutputStream zum Senden von Daten an den Server.
     * @param in DataInputStream zum Empfangen von Daten vom Server.
     */
    public LoginController(Stage stage, DataOutputStream out, DataInputStream in) {
        this.stage = stage;
        this.out = out;
        this.in = in;
        this.stage.setOnCloseRequest(this::handleWindowClose);
    }

    /**
     * Behandelt das Drücken der Enter-Taste, um den Login zu initiieren.
     * @param keyEvent Das KeyEvent, das mit dem Tastendruck verbunden ist.
     * @throws IOException Falls ein E/A-Fehler während des Logins auftritt.
     */
    @FXML
    protected void loginWithEnter(KeyEvent keyEvent) throws IOException {
        if ((keyEvent.getCode() == KeyCode.ENTER) && !(usernameField.getText().isBlank()) && !(passwordField.getText().isBlank())) {
            loginClick(null);
        }
    }

    /**
     * Behandelt das Klicken auf die Login-Schaltfläche.
     * @param event Das ActionEvent, das mit dem Klick auf die Login-Schaltfläche verbunden ist.
     * @throws IOException Falls ein E/A-Fehler während des Logins auftritt.
     */
    @FXML
    protected void loginClick(ActionEvent event) throws IOException {
        if (!(usernameField.getText().isEmpty() || passwordField.getText().isEmpty())) {
            out.writeUTF("auth:" + usernameField.getText() + ":" + passwordField.getText());
            String user = in.readUTF();

            if (!user.equals("null")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lobby-view.fxml"));
                loader.setControllerFactory(param -> new LobbyController(stage, out, in, user));
                Scene scene = new Scene(loader.load());

                stage.setScene(scene);
                stage.setTitle("Spielräume");
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
                stage.setResizable(false);
                stage.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Authentifizierungsfehler");
                alert.setHeaderText(null);
                alert.setContentText("Ihr Benutzername / Passwort ist falsch oder Sie sind bereits angemeldet!");
                alert.showAndWait();
            }
        } else {
            System.out.println("[Client] Benutzername oder Passwort leer");
        }
    }

    /**
     * Behandelt das Klicken auf die Registrieren-Schaltfläche.
     * @throws IOException Falls ein E/A-Fehler während der Registrierung auftritt.
     */
    @FXML
    protected void registerClick() throws IOException {
        Stage registerStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register-view.fxml"));
        loader.setControllerFactory(param -> new RegisterController(registerStage, out, in));
        Scene scene = new Scene(loader.load());

        registerStage.setScene(scene);
        registerStage.setTitle("Registrierung");
        registerStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        registerStage.setResizable(false);
        registerStage.initModality(Modality.APPLICATION_MODAL);
        registerStage.show();
    }

    /**
     * Behandelt das Schließen des Fensters.
     * @param event Das WindowEvent, das mit der Anforderung zum Schließen des Fensters verbunden ist.
     */
    private void handleWindowClose(WindowEvent event) {
        try {
            out.close();
            in.close();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

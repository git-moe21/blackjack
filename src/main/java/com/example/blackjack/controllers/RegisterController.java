package com.example.blackjack.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Controller-Klasse für die Registrierungsansicht der Anwendung.
 */
public class RegisterController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField ageField;
    @FXML
    private PasswordField passwordField;

    private Stage stage;
    private DataOutputStream out;
    private DataInputStream in;

    /**
     * Konstruktor für den RegisterController.
     * @param stage Die Hauptbühne der Anwendung.
     * @param out DataOutputStream zum Senden von Daten an den Server.
     * @param in DataInputStream zum Empfangen von Daten vom Server.
     */
    public RegisterController(Stage stage, DataOutputStream out, DataInputStream in) {
        this.stage = stage;
        this.out = out;
        this.in = in;
    }

    /**
     * Behandelt das Klicken auf den Zurück-Button.
     * Schließt das Registrierungsfenster.
     * @param event Das ActionEvent, das den Klick auslöst.
     */
    @FXML
    protected void backClick(ActionEvent event) {
        stage.close();
    }

    /**
     * Behandelt das Klicken auf den Registrieren-Button.
     * Validiert die Eingaben, sendet die Registrierungsdaten an den Server
     * und schließt das Registrierungsfenster bei erfolgreicher Registrierung.
     * Zeigt bei Fehlern entsprechende Fehlermeldungen an.
     * @param event Das ActionEvent, das den Klick auslöst.
     */
    @FXML
    protected void registerClick(ActionEvent event) {
        boolean registerComplete = false;
        String username = usernameField.getText();
        String password = passwordField.getText();
        int age;

        try {
            String ageString = ageField.getText();
            age = Integer.parseInt(ageString);

            // Server Abfrage
            out.writeUTF("dupuser:" + username);
            String duplicate = in.readUTF();

            if (!(duplicate.equals("true")) && (age > 5) && (!username.equals("")) && (!password.equals(""))) {
                out.writeUTF("reg:" + username + ":" + password); // Sende Registrierungsdaten an Server
                registerComplete = true;
            }

        } catch (NumberFormatException e) {
            // Fehler bei der Alters-Eingabe
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unzulässige Eingabe");
            alert.setHeaderText(null);
            alert.setContentText("Geben Sie bitte eine gültige ganze Zahl für Ihr Alter an!");
            alert.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException(e); // Allgemeiner IO-Fehler
        }

        if (registerComplete) {
            stage.close(); // Bei erfolgreicher Registrierung Fenster schließen
        } else {
            // Registrierung fehlgeschlagen, zeige Fehlermeldung
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unzulässige Eingabe");
            alert.setHeaderText(null);
            alert.setContentText("Registrierung fehlgeschlagen!");
            alert.showAndWait();
        }
    }
}

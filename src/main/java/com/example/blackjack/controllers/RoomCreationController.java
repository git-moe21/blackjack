package com.example.blackjack.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Controller-Klasse für die Raum-Erstellungsansicht der Anwendung.
 */
public class RoomCreationController {
    private Stage stage;
    private DataOutputStream out;
    private DataInputStream in;
    private String user;

    @FXML
    private TextField roomName;

    @FXML
    private ComboBox<Integer> easyBotsCombo;

    @FXML
    private ComboBox<Integer> hardBotsCombo;

    /**
     * Konstruktor für den RoomCreationController.
     * @param stage Die Hauptbühne der Anwendung.
     * @param out DataOutputStream zum Senden von Daten an den Server.
     * @param in DataInputStream zum Empfangen von Daten vom Server.
     * @param user Der aktuelle Benutzername.
     */
    public RoomCreationController(Stage stage, DataOutputStream out, DataInputStream in, String user) {
        this.stage = stage;
        this.out = out;
        this.in = in;
        this.user = user;
    }

    /**
     * Initialisiert die ComboBoxen für die Auswahl der Bots.
     */
    public void initialize() {
        ObservableList<Integer> items = FXCollections.observableArrayList(0, 1, 2, 3, 4, 5, 6, 7);
        easyBotsCombo.setItems(items);
        easyBotsCombo.setValue(0);
        hardBotsCombo.setItems(items);
        hardBotsCombo.setValue(0);
    }

    /**
     * Passt die verbleibenden Optionen der anderen ComboBox an, basierend auf der Auswahl in der ersten ComboBox.
     * @param item Die ausgewählte Anzahl der Bots in der ersten ComboBox.
     * @param comboBox Die ComboBox, deren Optionen angepasst werden sollen.
     */
    private void adjustItems(Integer item, ComboBox comboBox) {
        Integer remainingItems = 7 - item;
        ObservableList<Integer> remainingItemList = FXCollections.observableArrayList();
        for (int i = 0; i <= remainingItems; i++) {
            remainingItemList.add(i);
        }
        comboBox.setItems(remainingItemList);
    }

    /**
     * Wird aufgerufen, wenn in der ComboBox für einfache Bots eine Auswahl getroffen wird.
     * Passt die Optionen der ComboBox für schwierige Bots entsprechend an.
     */
    public void easyBotsSelected() {
        Integer selectedItem = easyBotsCombo.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            adjustItems(selectedItem, hardBotsCombo);
        }
    }

    /**
     * Wird aufgerufen, wenn in der ComboBox für schwierige Bots eine Auswahl getroffen wird.
     * Passt die Optionen der ComboBox für einfache Bots entsprechend an.
     */
    public void hardBotsSelected() {
        Integer selectedItem = hardBotsCombo.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            adjustItems(selectedItem, easyBotsCombo);
        }
    }

    /**
     * Behandelt das Klicken auf den "Raum erstellen"-Button.
     * Sendet die Raumdaten an den Server und schließt das Fenster bei erfolgreicher Erstellung.
     * Zeigt eine Fehlermeldung an, wenn der Raumname bereits vergeben ist.
     * @param event Das ActionEvent, das den Klick auslöst.
     * @throws IOException Falls ein Fehler beim Senden oder Empfangen der Daten auftritt.
     */
    @FXML
    protected void createRoomClick(ActionEvent event) throws IOException {
        if (!roomName.getText().isBlank()) {
            out.writeUTF("duproom:" + roomName.getText());
            out.flush();

            String answer = in.readUTF();

            if (answer.equals("false")) {
                out.writeUTF("addroom:" + roomName.getText() + ":" +
                        easyBotsCombo.getSelectionModel().getSelectedItem() + ":" +
                        hardBotsCombo.getSelectionModel().getSelectedItem());
                out.flush();
                stage.close();

            } else {
                System.out.println("[Client] Raumname bereits vergeben oder Table hat schonmal existiert");
            }
        }
    }

    /**
     * Behandelt das Klicken auf den Zurück-Button.
     * Schließt das Fenster für die Raum-Erstellung.
     * @param event Das ActionEvent, das den Klick auslöst.
     */
    @FXML
    protected void backClick(ActionEvent event) {
        stage.close();
    }
}

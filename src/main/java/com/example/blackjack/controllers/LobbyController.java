package com.example.blackjack.controllers;

import com.example.blackjack.utils.factories.ChatCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Controller-Klasse für die Lobby-Ansicht der Anwendung.
 */
public class LobbyController {
    @FXML
    private ListView<String> chatView;
    @FXML
    private TextField chatField;
    @FXML
    private ListView<String> leaderboardView;
    @FXML
    private ListView<String> activeUsersView;
    @FXML
    private ListView<String> activeRoomsView;

    public Stage stage;
    private DataOutputStream out;
    private DataInputStream in;
    private String user;
    private ArrayList<String> chat_messages;
    private Timer timer;

    /**
     * Konstruktor für den LobbyController.
     * @param stage Die Hauptbühne der Anwendung.
     * @param out DataOutputStream zum Senden von Daten an den Server.
     * @param in DataInputStream zum Empfangen von Daten vom Server.
     * @param user Der aktuelle Benutzername.
     */
    public LobbyController(Stage stage, DataOutputStream out, DataInputStream in, String user) {
        this.stage = stage;
        this.out = out;
        this.in = in;
        this.user = user;
        this.chat_messages = new ArrayList<>();

        this.stage.setOnCloseRequest(this::handleWindowClose);
    }

    /**
     * Initialisiert die Controller-Klasse nach dem Laden des FXML.
     * Setzt die Chat-Zellen-Fabrik und startet den Timer für die Aktualisierung der Lobby-Daten.
     */
    public void initialize() {
        chatView.setCellFactory(new ChatCellFactory());

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Platform.runLater(() -> {
                        try {
                            reloadScoreboard();
                            reloadActiveUsers();
                            reloadActiveRooms();
                            reloadChatMessages();
                        } catch (IOException e) {
                            timer.cancel();
                            timer.purge();
                        }
                    });
                } catch (Exception e) {
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 0, 200);
    }

    /**
     * Lädt die Daten für das Scoreboard neu vom Server.
     * @throws IOException Falls ein Fehler bei der Kommunikation mit dem Server auftritt.
     */
    private void reloadScoreboard() throws IOException {
        out.writeUTF("reloadscoreboard");
        out.flush();
        String scoreboard_string = in.readUTF();

        String[] scores = scoreboard_string.split("@");

        for (int i = 0; i < scores.length; i++) {
            String[] parts = scores[i].split(":");
            String user = parts[0];
            String points = parts[1];

            scores[i] = (user + " (" + points + ")");
        }

        ObservableList<String> scoreboard_items = FXCollections.observableArrayList(scores);
        leaderboardView.setItems(scoreboard_items);
    }

    /**
     * Lädt die Liste der aktiven Benutzer neu vom Server.
     * @throws IOException Falls ein Fehler bei der Kommunikation mit dem Server auftritt.
     */
    public void reloadActiveUsers() throws IOException {
        out.writeUTF("reloadactiveusers");
        out.flush();
        String active_users_string = in.readUTF();

        String[] active_users = active_users_string.split(":");
        ObservableList<String> active_user_items = FXCollections.observableArrayList(active_users);
        activeUsersView.setItems(active_user_items);
    }

    /**
     * Lädt die Liste der aktiven Räume neu vom Server.
     * @throws IOException Falls ein Fehler bei der Kommunikation mit dem Server auftritt.
     */
    private void reloadActiveRooms() throws IOException {
        out.writeUTF("reloadactiverooms");
        out.flush();
        String active_rooms_string = in.readUTF();

        if (!active_rooms_string.equals("")) {
            String[] active_rooms = active_rooms_string.split("@");

            for (int i = 0; i < active_rooms.length; i++) {
                String[] parts = active_rooms[i].split(":", 3);

                for (int j = 0; j < parts.length; j++) {
                    parts[j] = parts[j].replace(":", "; ");
                }


                String room_name = parts[0];
                String size = parts[1];

                if (parts.length == 2) {
                    active_rooms[i] = room_name + " || " + size;
                } else {
                    String players = parts[2].replace("#", "").replace("*", "");
                    active_rooms[i] = room_name + " || " + players + " || " + size;
                }
            }

            ObservableList<String> active_items = FXCollections.observableArrayList(active_rooms);
            activeRoomsView.setItems(active_items);

        } else {
            activeRoomsView.setItems(null);
        }
    }

    /**
     * Lädt die Chat-Nachrichten neu vom Server und aktualisiert die Ansicht.
     * @throws IOException Falls ein Fehler bei der Kommunikation mit dem Server auftritt.
     */
    private void reloadChatMessages() throws IOException {
        out.writeUTF("reloadchat");
        out.flush();
        String up_to_date_messages = in.readUTF();

        if (!up_to_date_messages.equals("")) {
            ArrayList<String> up_to_date_messages_arr = new ArrayList<>(Arrays.asList(up_to_date_messages.split("@")));

            // Ersetze User durch "Me" für CSS-Styling
            for (int i = 0; i < up_to_date_messages_arr.size(); i++) {
                String message = up_to_date_messages_arr.get(i);
                if (message.contains(user)) {
                    up_to_date_messages_arr.set(i, message.replace(user, "Me"));
                }
            }

            if (!up_to_date_messages_arr.equals(chat_messages)) {
                int index = chat_messages.size();

                for (int i = index; i < up_to_date_messages_arr.size(); i++) {
                    chatView.getItems().add(up_to_date_messages_arr.get(i));
                }

                chatView.scrollTo(up_to_date_messages_arr.size() - 1);
                chat_messages = up_to_date_messages_arr;
            }
        }
    }

    /**
     * Öffnet das Fenster zur Erstellung eines neuen Raums.
     * @param event Das ActionEvent, das den Aufruf ausgelöst hat.
     * @throws IOException Falls ein Fehler beim Laden des FXML auftritt.
     */
    @FXML
    protected void showRoomCreation(ActionEvent event) throws IOException {
        Stage roomStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/roomCreation-view.fxml"));
        loader.setControllerFactory(param -> new RoomCreationController(roomStage, out, in, user));
        Scene scene = new Scene(loader.load());

        roomStage.setScene(scene);
        roomStage.setTitle("Erstelle einen Raum");
        roomStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        roomStage.initModality(Modality.APPLICATION_MODAL);
        roomStage.setResizable(false);
        roomStage.show();
    }

    /**
     * Behandelt das Doppelklicken auf einen aktiven Raum zum Beitritt.
     * @param event Das MouseEvent, das den Doppelklick ausgelöst hat.
     */
    @FXML
    protected void joinRoom(MouseEvent event) throws IOException {
        String getRoom = activeRoomsView.getSelectionModel().getSelectedItem();

        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && (getRoom != null)) {
            String[] room_data = getRoom.split("\\|\\|");

            String room_name = room_data[0].trim();
            Integer size;

            try {
                size = Integer.parseInt(room_data[2].trim());

            } catch (ArrayIndexOutOfBoundsException e) {
                size = Integer.parseInt(room_data[1].trim());
            }


            if (size < 8) {
                out.writeUTF("joinroom:" + room_name + ":" + user);
                out.flush();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/room-view.fxml"));
                loader.setControllerFactory(param -> new RoomController(stage, out, in, user, room_name));
                Scene scene = new Scene(loader.load());

                stage.setScene(scene);
                stage.setTitle("Raum");
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
                stage.setResizable(false);
                stage.show();

                timer.cancel();
                timer.purge();

            } else {
                System.out.println("[Client] Raum ist voll");
            }
        }
    }

    /**
     * Behandelt das Klicken auf den Schnellspiel-Button.
     * @param event Das ActionEvent, das den Klick ausgelöst hat.
     */
    @FXML
    protected void joinQuickGame(ActionEvent event) throws IOException {
        ObservableList<String> rooms = activeRoomsView.getItems();

        if (rooms == null) {
            System.out.println("[Client] Es wurde kein aktiver Raum gefunden");

        } else {
            Random random = new Random();
            String room = rooms.get(random.nextInt(rooms.size()));
            String[] room_data = room.split("\\|\\|");

            String room_name = room_data[0].trim();
            Integer size;

            try {
                size = Integer.parseInt(room_data[2].trim());

            } catch (ArrayIndexOutOfBoundsException e) {
                size = Integer.parseInt(room_data[1].trim());
            }


            if (size < 8) {
                out.writeUTF("joinroom:" + room_name + ":" + user);
                out.flush();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/room-view.fxml"));
                loader.setControllerFactory(param -> new RoomController(stage, out, in, user, room_name));
                Scene scene = new Scene(loader.load());

                stage.setScene(scene);
                stage.setTitle("Raum");
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
                stage.setResizable(false);
                stage.show();

                timer.cancel();
                timer.purge();

            } else {
                System.out.println("[Client] Zufällig ausgewählter Raum ist voll. Probieren sie es nochmal");
            }
        }
    }

    /**
     * Behandelt das Klicken auf den Abmelden-Button.
     *
     * @param event Das ActionEvent, das den Klick ausgelöst hat.
     * @throws IOException Falls ein Fehler beim Abmelden auftritt.
     */
    @FXML
    protected void signoutClick(ActionEvent event) throws IOException {
        out.writeUTF("logout:" + user); // Logout an Server senden
        out.flush();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
        loader.setControllerFactory(param -> new LoginController(stage, out, in));
        Scene scene = new Scene(loader.load());

        stage.setScene(scene);
        stage.setTitle("Login");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        stage.setResizable(false);
        stage.show();

        timer.cancel();
        timer.purge();
    }

    /**
     * Behandelt das Senden einer Chat-Nachricht.
     * @param keyEvent Das KeyEvent, das das Drücken der Enter-Taste auslöst.
     * @throws IOException Falls ein Fehler beim Senden der Nachricht auftritt.
     */
    @FXML
    private void sendChatMessage(KeyEvent keyEvent) throws IOException {
        if ((keyEvent.getCode() == KeyCode.ENTER) && !(chatField.getText().isBlank())) {
            String message = chatField.getText();
            chatField.clear();

            out.writeUTF("chatmessage:" + this.user + ":" + message);
            out.flush();
        }
    }

    /**
     * Behandelt das Löschen des Benutzerkontos.
     * @param event Das ActionEvent, das das Löschen des Kontos auslöst.
     * @throws IOException Falls ein Fehler beim Löschen des Kontos auftritt.
     */
    @FXML
    private void deleteAccount(ActionEvent event) throws IOException {
        out.writeUTF("logout:" + user); // Logout an Server senden
        out.flush();
        out.writeUTF("delete:" + user);
        out.flush();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
        loader.setControllerFactory(param -> new LoginController(stage, out, in));
        Scene scene = new Scene(loader.load());

        stage.setScene(scene);
        stage.setTitle("Login");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        stage.setResizable(false);
        stage.show();

        timer.cancel();
        timer.purge();
    }

    /**
     * Behandelt das Schließen des Fensters.
     * @param event Das WindowEvent, das das Schließen des Fensters auslöst.
     */
    public void handleWindowClose(WindowEvent event) {
        try {
            out.writeUTF("logout:" + user); // Logout an Server senden
            out.flush();

            timer.cancel();
            timer.purge();

            out.close();
            in.close();

            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

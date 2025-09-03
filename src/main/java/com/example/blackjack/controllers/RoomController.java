package com.example.blackjack.controllers;

import com.example.blackjack.utils.factories.ChatCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Controller-Klasse zur Verwaltung der Raum-Funktionalität in einem Blackjack-Spiel.
 */
public class RoomController {
    @FXML
    private ListView<String> chatView; // ListView zur Anzeige der Chatnachrichten
    @FXML
    private TextField chatField; // TextField zur Eingabe von Chatnachrichten
    @FXML
    private Label roomLabel;
    @FXML
    private ListView<String> playerListView, easyBotListView, hardBotListView;
    private Stage stage; // Die aktuelle Stage
    private DataOutputStream out; // OutputStream zum Senden von Daten an den Server
    private DataInputStream in; // InputStream zum Empfangen von Daten vom Server
    private String user; // Der Benutzername des aktuellen Benutzers
    private String roomname; // Der Name des aktuellen Chatraums
    private Timer timer; // Timer für periodische Aufgaben
    private ArrayList<String> chat_messages; // Liste der Chatnachrichten

    /**
     * Konstruktor für RoomController.
     *
     * @param stage Die aktuelle Stage
     * @param out OutputStream zum Senden von Daten an den Server
     * @param in InputStream zum Empfangen von Daten vom Server
     * @param user Der Benutzername des aktuellen Benutzers
     * @param roomname Der Name des aktuellen Chatraums
     */
    public RoomController(Stage stage, DataOutputStream out, DataInputStream in, String user, String roomname) {
        this.stage = stage;
        this.out = out;
        this.in = in;
        this.user = user;
        this.roomname = roomname;
        this.chat_messages = new ArrayList<>();

        this.stage.setOnCloseRequest(this::handleWindowClose);
    }

    /**
     * Initialisiert den Controller.
     */
    public void initialize() {
        roomLabel.setText("Raum-" + roomname);

        chatView.setCellFactory(new ChatCellFactory());

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Platform.runLater(() -> {
                        try {
                            reloadGameStart();
                            reloadUsersInRoom();
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
     * Sendet eine Chatnachricht, wenn die Eingabetaste gedrückt wird.
     *
     * @param keyEvent Das KeyEvent, das ausgelöst wurde
     * @throws IOException Wenn ein E/A-Fehler auftritt
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
     * Handhabt das Klicken auf den Zurück-Button und verlässt den Raum.
     *
     * @param event Das ActionEvent, das ausgelöst wurde
     * @throws IOException Wenn ein E/A-Fehler auftritt
     */
    @FXML
    protected void backClick(ActionEvent event) throws IOException {
        out.writeUTF("leaveroom:" + roomname + ":" + user);
        out.flush();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lobby-view.fxml"));
        loader.setControllerFactory(param -> new LobbyController(stage, out, in, user));
        Scene scene = new Scene(loader.load());

        stage.setScene(scene);
        stage.setTitle("Spielräume");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        stage.setResizable(false);
        stage.show();

        timer.cancel();
        timer.purge();
    }

    /**
     * Handhabt das Klicken auf den Start-Button und startet das Spiel.
     *
     * @param event Das ActionEvent, das ausgelöst wurde
     * @throws IOException Wenn ein E/A-Fehler auftritt
     */
    @FXML
    protected void startClick(ActionEvent event) throws IOException {
        timer.cancel();
        timer.purge();

        out.writeUTF("startgame:" + roomname);
        out.flush();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game-view.fxml"));
        loader.setControllerFactory(param -> new GameController(stage, out, in, user, roomname));
        Scene scene = new Scene(loader.load());

        stage.setScene(scene);
        stage.setTitle("Blackjack");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Lädt die Chatnachrichten neu.
     *
     * @throws IOException Wenn ein E/A-Fehler auftritt
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
     * Lädt die Benutzer im Raum neu.
     */
    private void reloadUsersInRoom() throws IOException {
        out.writeUTF("reloadroominfo:" + roomname);
        out.flush();

        String info = in.readUTF();
        String[] tmp = info.split(":");

        if (!(tmp.length == 1)) {
            String[] players_with_bots = Arrays.copyOfRange(tmp, 2, tmp.length);

            ObservableList<String> easyBotsArray = FXCollections.observableArrayList();
            ObservableList<String> hardBotsArray = FXCollections.observableArrayList();
            ObservableList<String> PlayersArray = FXCollections.observableArrayList();

            for (String p : players_with_bots) {
                if (p.startsWith("#")) {
                    easyBotsArray.add(p.replace("#", ""));
                } else if (p.startsWith("*")) {
                    hardBotsArray.add(p.replace("*", ""));
                } else {
                    PlayersArray.add(p);
                }
            }

            playerListView.setItems(PlayersArray);
            easyBotListView.setItems(easyBotsArray);
            hardBotListView.setItems(hardBotsArray);
        }
    }

    /**
     * Schaut ob das Game bereits durch einen anderen Spieler gestartet wurde
     */
    private void reloadGameStart() throws IOException {
        out.writeUTF("started:" + roomname);
        out.flush();

        if (in.readUTF().equals("true")) {
            timer.cancel();
            timer.purge();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game-view.fxml"));
            loader.setControllerFactory(param -> new GameController(stage, out, in, user, roomname));
            Scene scene = new Scene(loader.load());

            stage.setScene(scene);
            stage.setTitle("Blackjack");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
            stage.setResizable(false);
            stage.show();
        }
    }

    /**
     * Entfernt einen simpleBot aus dem Spielraum
     *
     * @throws IOException Wenn ein E/A-Fehler auftritt
     */
    public void removeSimpleBot(ActionEvent actionEvent) throws IOException {
        out.writeUTF("removesimplebot:" + roomname);
        out.flush();
    }

    /**
     * Entfernt einen HardBot aus dem Spielraum
     *
     * @throws IOException Wenn ein E/A-Fehler auftritt
     */
    public void removeHardBot(ActionEvent actionEvent) throws IOException {
        out.writeUTF("removehardbot:" + roomname);
        out.flush();
    }

    /**
     * Handhabt das Schließen des Fensters.
     *
     * @param event Das WindowEvent, das ausgelöst wurde
     */
    private void handleWindowClose(WindowEvent event) {
        try {
            out.writeUTF("leaveroom:" + roomname + ":" + user);
            out.flush();

            out.writeUTF("logout:" + user); // Logout an den Server senden
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

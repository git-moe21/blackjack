package com.example.blackjack.utils;

import com.example.blackjack.controllers.LoginController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;

/**
 * Diese Klasse stellt einen Client dar, der eine Verbindung zu einem Server aufbaut
 * und ein Anmeldefenster startet. Der Client kommuniziert über einen Socket mit dem Server.
 */
public class Client implements Runnable {
    private Stage stage;
    private Socket socket;
    private String serverName;
    private int port;
    private DataOutputStream out;
    private DataInputStream in;

    /**
     * Konstruktor für die Client-Klasse. Initialisiert die Verbindungsparameter.
     *
     * @param stage das JavaFX-Stageobjekt für die Benutzeroberfläche.
     * @param serverName der Name oder die IP-Adresse des Servers, zu dem eine Verbindung hergestellt werden soll.
     * @param port der Port, auf dem der Server lauscht.
     */
    public Client(Stage stage, String serverName, int port) {
        this.stage = stage;
        this.serverName = serverName;
        this.port = port;
    }

    /**
     * Startet den Client und stellt eine Verbindung zum Server her. Initialisiert die In- und OutputStreams
     * und öffnet das Anmeldefenster.
     *
     * Diese Methode wird in einem separaten Thread ausgeführt.
     *
     * Anforderungen:
     * - Der Server muss verfügbar und erreichbar sein.
     * - Die Ressourcendateien (FXML und Icon) müssen im angegebenen Pfad vorhanden sein.
     *
     * Ausnahmen:
     * @throws Exception wenn ein Fehler beim Verbindungsaufbau, beim Laden der FXML-Datei oder beim Setzen der Szene auftritt.
     */
    @Override
    public void run() {
        Platform.runLater(() -> {
            try {
                // Verbindungsaufbau
                this.socket = new Socket(serverName, port);
                this.out = new DataOutputStream(socket.getOutputStream());
                this.in = new DataInputStream(socket.getInputStream());

                //System.out.println("[Client] Verbunden mit " + socket.getRemoteSocketAddress());

                // Start des Anmeldefensters
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
                loader.setControllerFactory(param -> new LoginController(stage, out, in));
                Scene scene = new Scene(loader.load());

                stage.setScene(scene);
                stage.setTitle("Login");
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
                stage.setResizable(false);
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

package com.example.blackjack.views;

import com.example.blackjack.utils.Client;
import com.example.blackjack.utils.server.Server;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Die Main-Klasse startet den Server und mehrere Clients für das Blackjack-Spiel.
 * Sie erweitert die JavaFX Application und implementiert die start() und stop() Methoden.
 */
public class Main extends Application {
    private ExecutorService executor;

    /**
     * Startet den Server und mehrere Clients für das Blackjack-Spiel.
     *
     * @param primaryStage die primäre Stage für die JavaFX-Anwendung
     * @throws IOException wenn ein I/O-Fehler auftritt
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        TextField numberInput = new TextField();
        numberInput.setPromptText("Geben Sie eine Zahl ein");

        // Label für die Anzeige der eingegebenen Zahl
        Label numberLabel = new Label();
        numberLabel.setText("Gebe die Anzahl an extra Clients ein!");

        // Knopf
        Button submitButton = new Button("Absenden");
        // Aktion für den Knopf
        submitButton.setOnAction(event -> {
            String input = numberInput.getText();
            try {
                int extra_clients = Integer.parseInt(input);
                executor = Executors.newFixedThreadPool(extra_clients + 2);

                // Server Start
                Server server = new Server(5000, extra_clients);
                executor.submit(server);

                // Client Start
                Client client = new Client(primaryStage, "localhost", 5000);
                executor.submit(client);

                // Zusätzliche Clients starten
                for (int i = 0; i < extra_clients; i++) {
                    executor.submit(new Client(new Stage(), "localhost", 5000));
                }
            } catch (NumberFormatException e) {
                numberLabel.setText("Bitte eine gültige Zahl eingeben!");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Layout
        VBox layout = new VBox(10);
        layout.getChildren().addAll(numberLabel, numberInput, submitButton);

        // Szene und Bühne
        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Clients starten");
        primaryStage.show();
    }

    /**
     * Stoppt die Ausführung der Anwendung.
     * Beendet den ExecutorService, um alle Threads sauber zu beenden.
     */
    @Override
    public void stop() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    /**
     * Die main-Methode startet die JavaFX-Anwendung.
     *
     * @param args die Argumente für die Anwendung (nicht verwendet)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
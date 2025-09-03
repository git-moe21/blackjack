package com.example.blackjack.utils.server;

import com.example.blackjack.utils.game.exceptions.DeckEmptyException;
import com.example.blackjack.utils.game.exceptions.InvalidMoveException;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Der ClientHandler behandelt die Kommunikation mit einem einzelnen Client in einem Blackjack-Server.
 * Er verarbeitet eingehende Anfragen des Clients und führt entsprechende Aktionen durch.
 */
public class ClientHandler extends Thread {
    private Socket socket; // Die Socket-Verbindung zum Client
    private Database database; // Die Datenbank für die Benutzer- und Rauminformationen
    private DataInputStream in; // Input-Stream zum Lesen von Daten vom Client
    private DataOutputStream out; // Output-Stream zum Senden von Daten zum Client

    /**
     * Konstruktor für ClientHandler.
     *
     * @param socket Die Socket-Verbindung zum Client
     * @param database Die Datenbank, die vom Server verwendet wird
     * @throws IOException Wenn ein Fehler beim Einrichten der Streams auftritt
     */
    public ClientHandler(Socket socket, Database database) throws IOException {
        this.socket = socket;
        this.database = database;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * Hauptmethode, die während der Thread-Ausführung läuft.
     * Sie wartet auf eingehende Anfragen vom Client und führt entsprechende Aktionen aus.
     */
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                waitForRequest();
            }
        } catch (IOException e) {
            // Client schließt bei Logout oder Fensterschließung die Socket-Verbindung,
            // es kommt zur IOException und der ClientHandler wird beendet
            System.out.println("[Server] Verbindung (" + socket.getRemoteSocketAddress() + ") wird geschlossen");
        } catch (InvalidMoveException ex) {
            System.out.println("[Server] Ein Spieler hat versucht einen zu hohen Einsatz zu setzen");

        } catch (DeckEmptyException e) {
            System.out.println("[Server] Ein Spieler hat versucht eine Karte aus einem leeren Deck zu ziehen");
        } catch (InterruptedException e) {
            System.out.println("[Server] Beim Neustarten eines Spieltisches kam es zu einem Fehler");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Wartet auf eine Anfrage vom Client und führt die entsprechende Aktion aus.
     *
     * @throws IOException Wenn ein Fehler beim Lesen oder Schreiben von Daten auftritt
     */
    private void waitForRequest() throws IOException, InvalidMoveException, DeckEmptyException, InterruptedException {
        String input = in.readUTF();

        // Authentifizierung
        if (input.startsWith("auth")) {
            String[] data = input.split(":");

            if (data.length == 3) {
                String username = data[1];
                String password = data[2];
                out.writeUTF(String.valueOf(authenticateUser(username, password)));
                out.flush();
            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Überprüfung, ob Benutzername bereits vergeben ist (Registrierung)
        if (input.startsWith("dupuser")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String username = data[1];
                out.writeUTF(String.valueOf(duplicateUsername(username)));
                out.flush();
            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Registrierung eines neuen Benutzers
        if (input.startsWith("reg")) {
            String[] data = input.split(":");

            if (data.length == 3) {
                String username = data[1];
                String password = data[2];
                addUser(username, password);
            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Löschen eines Benutzers
        if (input.startsWith("delete")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String user = data[1];
                deleteUser(user);
            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Logout eines Benutzers
        if (input.startsWith("logout")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String user = data[1];
                logout(user);
            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Liste der aktiven Benutzer senden
        if (input.startsWith("reloadactiveusers")) {
            out.writeUTF(getActiveUsers());
            out.flush();
        }

        // Liste der aktiven Räume senden
        if (input.startsWith("reloadactiverooms")) {
            out.writeUTF(getActiveRooms());
            out.flush();
        }

        // Aktuelles Scoreboard senden
        if (input.startsWith("reloadscoreboard")) {
            out.writeUTF(getScoreboard());
            out.flush();
        }

        // Chatnachricht senden
        if (input.startsWith("chatmessage")) {
            String[] data = input.split(":");
            if (data.length == 3) {
                saveChatMessage(data[1], data[2]);
            }
        }

        // Chatnachrichten empfangen
        if (input.startsWith("reloadchat")) {
            StringBuilder messages_string = new StringBuilder();
            for (String message : getChatMessages()) {
                messages_string.append(message).append("@");
            }
            out.writeUTF(messages_string.toString());
            out.flush();
        }

        // Raum erstellen
        if (input.startsWith("addroom")) {
            String[] data = input.split(":");

            if (data.length == 4) {
                String roomname = data[1];
                String simple_bot_count = data[2];
                String advanced_bot_count = data[3];
                addRoom(roomname, Integer.parseInt(simple_bot_count), Integer.parseInt(advanced_bot_count));

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Überprüfen, ob Raumname bereits vorhanden ist
        if (input.startsWith("duproom")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String roomname = data[1];
                out.writeUTF(String.valueOf(duplicateRoomName(roomname)));
                out.flush();
            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Einem Raum beitreten
        if (input.startsWith("joinroom")) {
            String[] data = input.split(":");

            if (data.length == 3) {
                String roomname = data[1];
                String username = data[2];

                joinRoom(roomname, username);

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Einen Raum verlassen
        if (input.startsWith("leaveroom")) {
            String[] data = input.split(":");

            if (data.length == 3) {
                String roomname = data[1];
                String username = data[2];

                leaveRoom(roomname, username);

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Einen Raum verlassen
        if (input.startsWith("reloadroominfo")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String roomname = data[1];

                out.writeUTF(reloadRoomInfo(roomname));
                out.flush();

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Simple Bot aus Raum werfen
        if (input.startsWith("removesimplebot")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String roomname = data[1];

                removeSimpleBot(roomname);

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Hard Bot aus Raum werfen
        if (input.startsWith("removehardbot")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String roomname = data[1];

                removeHardBot(roomname);

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Spielstart
        if (input.startsWith("startgame")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String roomname = data[1];

                startGame(roomname);

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Spielstart bereits geschehen
        if (input.startsWith("started")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String roomname = data[1];

                out.writeUTF(String.valueOf(hasGameStarted(roomname)));
                out.flush();

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Initialisiere Spieler mit ihren Informationen
        if (input.startsWith("initializegame")) {
            String[] data = input.split(":");

            if (data.length == 3) {
                String table_name = data[1];
                String user_name = data[2];

                out.writeUTF(initializeGame(table_name, user_name));

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Aktuellsten Spielstand bekommen
        if (input.startsWith("reloadgamestate")) {
            String[] data = input.split(":");

            if (data.length == 3) {
                String table_name = data[1];
                String user_name = data[2];

                out.writeUTF(getGameState(table_name, user_name));

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Tisch verlassen
        if (input.startsWith("leavetable")) {
            String[] data = input.split(":");

            if (data.length == 3) {
                String table_name = data[1];
                String user_name = data[2];

                leaveTable(table_name, user_name);

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Einsatz setzen
        if (input.startsWith("setstake")) {
            String[] data = input.split(":");

            if (data.length == 4) {
                String table_name = data[1];
                String user_name = data[2];
                int stake = Integer.parseInt(data[3]);

                setStake(table_name, user_name, stake);

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Karte ziehen
        if (input.startsWith("hit")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String table_name = data[1];

                hit(table_name);

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Stand Move
        if (input.startsWith("stand")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String table_name = data[1];

                stand(table_name);

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Doubledown Move
        if (input.startsWith("doubledown")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String table_name = data[1];

                doubledown(table_name);

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Split Move
        if (input.startsWith("split")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String table_name = data[1];

                split(table_name);

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Surrender Move
        if (input.startsWith("surrender")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String table_name = data[1];

                surrender(table_name);

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }

        // Alle Score von aktiven Spieler bekommen
        if (input.startsWith("getallscores")) {
            String[] data = input.split(":");

            if (data.length == 2) {
                String table_name = data[1];

                out.writeUTF(getAllScores(table_name));

            } else {
                System.out.println("[Server] Format Fehler");
            }
        }
    }

    /**
     * Authentifiziert einen Benutzer anhand des Benutzernamens und Passworts in der Datenbank.
     *
     * @param username Der Benutzername
     * @param password Das Passwort des Benutzers
     * @return Der Token des authentifizierten Benutzers oder "null" bei fehlerhaften Anmeldeinformationen
     */
    private String authenticateUser(String username, String password) {
        return database.authenticate(username, password);
    }

    /**
     * Überprüft, ob ein Benutzername bereits in der Datenbank vorhanden ist.
     *
     * @param username Der zu überprüfende Benutzername
     * @return true, wenn der Benutzername bereits existiert, sonst false
     */
    private boolean duplicateUsername(String username) {
        return database.duplicateUsername(username);
    }

    /**
     * Meldet einen Benutzer ab (entfernt den Benutzer aus der Liste der aktiven Benutzer).
     *
     * @param token Der Token des Benutzers, der abgemeldet wird
     */
    private void logout(String token) {
        database.removeActiveUser(token);
    }

    /**
     * Fügt einen neuen Benutzer zur Datenbank hinzu.
     *
     * @param username Der Benutzername des neuen Benutzers
     * @param password Das Passwort des neuen Benutzers
     */
    private void addUser(String username, String password) {
        database.addUser(username, password);
    }

    /**
     * Löscht einen Benutzer aus der Datenbank.
     *
     * @param user Der Benutzername des zu löschenden Benutzers
     */
    private void deleteUser(String user) {
        database.deleteUser(user);
    }

    /**
     * Gibt eine Liste der aktiven Benutzer zurück.
     *
     * @return Eine Zeichenfolge, die die aktiven Benutzer enthält
     */
    private String getActiveUsers() {
        return database.getActiveUsers();
    }

    /**
     * Gibt eine Liste der aktiven Räume zurück.
     *
     * @return Eine Zeichenfolge, die die aktiven Räume enthält
     */
    private String getActiveRooms() {
        return database.getActiveRooms();
    }

    /**
     * Fügt einen neuen Raum zur Datenbank hinzu.
     *
     * @param roomname Der Name des neuen Raums
     * @param simple_bots_count Die Anzahl der einfachen Bots im Raum
     * @param advanced_bots_count Die Anzahl der fortgeschrittenen Bots im Raum
     */

    private void addRoom(String roomname, int simple_bots_count, int advanced_bots_count) throws InvalidMoveException {
        database.addRoom(roomname, simple_bots_count, advanced_bots_count);
    }

    /**
     * Fügt einen Benutzer einem Raum in der Datenbank hinzu.
     *
     * @param roomname Der Name des Raums, dem der Benutzer beitritt
     * @param username Der Benutzername des Benutzers, der dem Raum beitritt
     */
    private void joinRoom(String roomname, String username) {
        database.joinRoom(roomname, username);
    }

    /**
     * Entfernt einen Benutzer aus einem Raum in der Datenbank.
     *
     * @param roomname Der Name des Raums, aus dem der Benutzer austreten möchte
     * @param username Der Benutzername des Benutzers, der den Raum verlässt
     */
    private void leaveRoom(String roomname, String username) {
        database.leaveRoom(roomname, username);
    }

    /**
     * Entfernt einen Simple Bot aus dem Raum
     *
     * @param roomname Der Name des Raums, aus dem der Bot rausgeworfen werden soll
     */
    private void removeSimpleBot(String roomname) {
        database.removeSimpleBot(roomname);
    }

    /**
     * Entfernt einen Hard Bot aus dem Raum
     *
     * @param roomname Der Name des Raums, aus dem der Bot rausgeworfen werden soll
     */
    private void removeHardBot(String roomname) {
        database.removeHardBot(roomname);
    }

    /**
     * Gibt die aktuellen Spieler im Raum zurück
     *
     * @param room_name Der zu überprüfende Raum
     * @return String der die aktuellen Spieler beeinhaltet
     */
    private String reloadRoomInfo(String room_name) {
        return database.reloadRoomInfo(room_name);
    }

    /**
     * Überprüft, ob ein Raumname bereits in der Datenbank vorhanden ist.
     *
     * @param room_name Der zu überprüfende Raumname
     * @return true, wenn der Raumname bereits existiert, sonst false
     */
    private boolean duplicateRoomName(String room_name) {
        return database.duplicateRoomName(room_name);
    }

    /**
     * Startet das Spiel von Raum "room_name"
     *
     * @param room_name Der zu startende Raum
     */
    private void startGame(String room_name) throws InvalidMoveException {
        database.startGame(room_name);
    }

    /**
     * Schaut ob das Spiel von "room_name" bereits gestartet wurde
     *
     * @param room_name Der zu überprüfende Raum
     * @return true, wenn das Spiel gestartet wurde, sonst false
     */
    private boolean hasGameStarted(String room_name) {
        return database.hasGameStarted(room_name);
    }

    /**
     * Entfernt einen Spieler von einem Tisch.
     *
     * @param table_name Der Name des Tischs, aus dem der Spieler austreten möchte
     * @param user_name Der Benutzername des Spielers, der den Tisch verlässt
     */
    private void leaveTable(String table_name, String user_name) throws DeckEmptyException, InterruptedException {
        database.leaveTable(table_name, user_name);
    }

    /**
     * Gibt aktuellsten Game State zu table_name zurück
     *
     * @param table_name Der zu überprüfende Tisch
     * @return GameState
     */
    private String getGameState(String table_name, String user_name) {
        return database.getGameState(table_name, user_name);
    }

    /**
     * Initialisert Spieler und ihre Bilder
     *
     * @param table_name Der zu überprüfende Tisch
     * @param user_name
     * @return Initial Game State
     */
    private String initializeGame(String table_name, String user_name) {
        return database.initializeGame(table_name, user_name);
    }

    /**
     * Setz den Einsatz eines Spielers
     *
     * @param table_name Der Tisch an dem der Spieler spielt
     * @param user_name Spieler der seinen Einsatz setzen will
     * @param stake Zu setzender Einsatz
     */
    private void setStake(String table_name, String user_name, int stake) throws InvalidMoveException {
        database.setStake(table_name, user_name, stake);
    }

    /**
     * Ziehe eine Karte (Hit), kann durch Client Restriktionen nur durch den im Moment aktiven Spieler aufgerufen werden
     *
     * @param table_name Der Tisch an dem der Spieler spielt
     */
    private void hit(String table_name) throws DeckEmptyException, InterruptedException {
        database.hit(table_name);
    }

    /**
     * Keine Karte ziehen
     *
     * @param table_name Der Tisch an dem der Spieler spielt
     */
    private void stand(String table_name) throws DeckEmptyException, InterruptedException {
        database.stand(table_name);
    }

    /**
     * Doubledown Spielzug
     *
     * @param table_name Der Tisch an dem der Spieler spielt
     */
    private void doubledown(String table_name) throws DeckEmptyException, InterruptedException {
        database.doubledown(table_name);
    }

    /**
     * Split Spielzug
     *
     * @param table_name Der Tisch an dem der Spieler spielt
     */
    private void split(String table_name) throws DeckEmptyException, InterruptedException {
        database.split(table_name);
    }

    /**
     * Anfangs Blatt verwerfen und aufgeben (surrender)
     *
     * @param table_name Der Tisch an dem der Spieler spielt
     */
    private void surrender(String table_name) throws DeckEmptyException, InterruptedException {
        database.surrender(table_name);
    }

    /**
     * Gibt alle Scores von den aktiven Spieler zurück
     *
     * @param table_name Der zu überprüfende Tisch
     * @return Alle Scores von Spielern
     */
    private String getAllScores(String table_name) {
        return database.getAllScores(table_name);
    }

    /**
     * Gibt das aktuelle Scoreboard (Punktestand-Tabelle) zurück.
     *
     * @return Eine Zeichenfolge, die das Scoreboard enthält
     */
    private String getScoreboard() {
        return database.getScoreboard();
    }

    /**
     * Speichert eine Chatnachricht in der Datenbank.
     *
     * @param user Der Benutzername des Absenders der Nachricht
     * @param message Der Inhalt der Chatnachricht
     */
    private void saveChatMessage(String user, String message) {
        database.saveChatMessage(user, message);
    }

    /**
     * Holt eine Liste der Chatnachrichten aus der Datenbank.
     *
     * @return Eine ArrayList von Zeichenfolgen, die die Chatnachrichten enthält
     */
    private ArrayList<String> getChatMessages() {
        return database.getChatMessages();
    }
}

package com.example.blackjack.utils.server;

import com.example.blackjack.utils.game.*;
import com.example.blackjack.utils.game.exceptions.DeckEmptyException;
import com.example.blackjack.utils.game.exceptions.InvalidMoveException;
import com.example.blackjack.utils.game.player.Player;
import com.example.blackjack.utils.game.player.bots.AdvancedBot;
import com.example.blackjack.utils.game.player.bots.SimpleBot;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Die Database-Klasse verwaltet Benutzerdaten, aktive Benutzer, aktive Räume, Scoreboard und Chat-Nachrichten.
 * Sie bietet Methoden zur Benutzer-Authentifizierung, Raumverwaltung und Datenpersistenz.
 */
public class Database {
    private final Set<User> users;
    private final Set<User> active_users;
    private final ArrayList<Room> active_rooms;
    private final ArrayList<Table> active_tables;
    private ArrayList<Score> scoreboard;
    private final ArrayList<String> chat;
    private String jarDir;

    /**
     * Konstruktor initialisiert die Datenbank und lädt Benutzer und Scoreboard aus der Datei.
     */
    public Database() {
        this.users = new HashSet<>();
        this.active_users = new HashSet<>();
        this.active_rooms = new ArrayList<>();
        this.active_tables = new ArrayList<>();
        this.scoreboard = new ArrayList<>();
        this.chat = new ArrayList<>();
        initializeFiles();
        loadUsersFromFile();
        loadScoreboardFromFile();
    }

    public void initializeFiles() {
        String loginResourcePath = "/database/login.txt";
        String scoreboardResourcePath = "/database/scoreboard.txt";

        // Namen der temporären Dateien
        String tempLoginFileName = "temp_login.txt";
        String tempScoreboardFileName = "temp_scoreboard.txt";

        // Verzeichnis der JAR-Datei ermitteln
        this.jarDir = getJarDir();

        if (jarDir == null || new File(jarDir + File.separator + tempLoginFileName).exists() || new File(jarDir + File.separator + tempScoreboardFileName).exists()) {
            //System.out.println("Verzeichnis der JAR-Datei konnte nicht ermittelt werden oder temp Files bereits vorhanden");
            return;
        }

        // Vollständige Pfade der temporären Dateien
        String tempLoginFilePath = jarDir + File.separator + tempLoginFileName;
        String tempScoreboardFilePath = jarDir + File.separator + tempScoreboardFileName;

        // Ressourcen-Dateien in temporäre Dateien kopieren
        copyResourceToFile(loginResourcePath, tempLoginFilePath);
        copyResourceToFile(scoreboardResourcePath, tempScoreboardFilePath);
    }

    private String getJarDir() {
        try {
            String jarPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            return new File(jarPath).getParent();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void copyResourceToFile(String resourceFileName, String destFileName) {
        try (InputStream inputStream = this.getClass().getResourceAsStream(resourceFileName);
             OutputStream outputStream = new FileOutputStream(destFileName)) {

            if (inputStream == null) {
                System.out.println("Ressourcendatei nicht gefunden: " + resourceFileName);
                return;
            }

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --------------------------------- User Methoden ---------------------------------

    /**
     * Fügt einen neuen Benutzer zur Datenbank hinzu.
     *
     * @param username der Benutzername des neuen Benutzers
     * @param password das Passwort des neuen Benutzers
     * @throws IllegalArgumentException wenn der Benutzername oder das Passwort null oder leer ist
     */
    public synchronized void addUser(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Benutzername und Passwort dürfen nicht null oder leer sein.");
        }

        users.add(new User(username, password));
        System.out.println("[Server] Benutzer (" + username + ") wurde erfolgreich angelegt");
        saveUsersToFile();

        scoreboard.add(new Score(username, 500));
        saveScoreboardToFile();
    }

    /**
     * Löscht einen Benutzer aus der Datenbank.
     *
     * @param user der Benutzername des zu löschenden Benutzers
     * @throws IllegalArgumentException wenn der Benutzername null oder leer ist
     */
    public synchronized void deleteUser(String user) {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("Benutzername darf nicht null oder leer sein.");
        }
        for (User u : users) {
            if (u.getUsername().equals(user)) {
                System.out.println("[Server] " + user + " wurde erfolgreich aus der Datenbank gelöscht");
                users.remove(u);
                break;
            }
        }
        saveUsersToFile();

        for (Score s : scoreboard) {
            if (s.getUser().equals(user)) {
                scoreboard.remove(s);
                break;
            }
        }
        saveScoreboardToFile();
    }

    /**
     * Überprüft, ob ein Benutzername bereits in der Datenbank existiert.
     *
     * @param username der zu überprüfende Benutzername
     * @return true, wenn der Benutzername bereits existiert, andernfalls false
     * @throws IllegalArgumentException wenn der Benutzername null oder leer ist
     */
    public boolean duplicateUsername(String username) {
        loadUsersFromFile();
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Benutzername darf nicht null oder leer sein.");
        }
        for (User u : users) {
            if (Objects.equals(u.getUsername(), username)) {
                System.out.println("[Server] Registrierung mit bereits vergebenem Benutzernamen verhindert");
                return true;
            }
        }
        return false;
    }

    /**
     * Authentifiziert einen Benutzer basierend auf dem angegebenen Benutzernamen und Passwort.
     *
     * @param username der Benutzername des Benutzers
     * @param password das Passwort des Benutzers
     * @return den Benutzernamen, wenn die Authentifizierung erfolgreich ist, andernfalls null
     * @throws IllegalArgumentException wenn der Benutzername oder das Passwort null oder leer ist
     */
    public synchronized String authenticate(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Benutzername und Passwort dürfen nicht null oder leer sein.");
        }
        loadUsersFromFile();
        for (User u : users) {
            if ((u.getUsername().equals(username)) && (u.getPassword().equals(password))) {
                if (!active_users.contains(u)) {
                    System.out.println("[Server] " + username + " hat sich erfolgreich angemeldet");
                    active_users.add(u);
                    return username;
                } else {
                    System.out.println("[Server] Doppel Login (" + username + ") verhindert");
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Gibt eine Liste der aktiven Benutzer zurück.
     *
     * @return ein durch Doppelpunkte getrenntes String der aktiven Benutzernamen
     */
    public String getActiveUsers() {
        StringBuilder active_users_names = new StringBuilder();
        for (User u : active_users) {
            active_users_names.append(u.getUsername()).append(":");
        }
        return active_users_names.toString();
    }

    /**
     * Entfernt einen User aus der Liste der aktiven User.
     *
     * @param user der Benutzername des zu entfernenden Benutzers
     * @throws IllegalArgumentException wenn der Benutzername null oder leer ist
     */
    public synchronized void removeActiveUser(String user) {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("Benutzername darf nicht null oder leer sein.");
        }
        boolean successful = false;
        for (User u : active_users) {
            if (u.getUsername().equals(user)) {
                System.out.println("[Server] " + user + " hat sich abgemeldet");
                successful = true;
                active_users.remove(u);
                break;
            }
        }
        if (!successful) {
            System.out.println("[Server] Dieser Spieler dürfte nicht angemeldet sein");
        }
    }

    // --------------------------------- Raum Methoden ---------------------------------

    /**
     * Fügt einen neuen Raum zu den aktiven Räumen hinzu.
     *
     * @param name der Name des neuen Raums
     * @throws IllegalArgumentException wenn der Name null oder leer ist
     */
    public synchronized void addRoom(String name, int simple_bot_count, int advanced_bot_count) throws InvalidMoveException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Raumname darf nicht null oder leer sein.");
        }

        active_rooms.add(new Room(name, simple_bot_count, advanced_bot_count, this));
    }

    /**
     * Überprüft, ob ein Raumname bereits in der Liste der aktiven Räume existiert und ob es einen Raum mit dem Namen gibt der bereits gestartet wurde.
     *
     * @param name der zu überprüfende Raumname
     * @return true, wenn Fehler, andernfalls false
     */
    public boolean duplicateRoomName(String name) {
        for (Room r : active_rooms) {
            if (r.getRoom_name().equals(name)) {
                return true;
            }
        }
        for (Table t : active_tables) {
            if (t.getTableName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Fügt einen Benutzer einem Raum hinzu.
     *
     * @param roomname der Name des Raums, zu dem der Benutzer hinzugefügt wird
     * @param username der Benutzername des Benutzers, der dem Raum beitritt
     */
    public synchronized void joinRoom(String roomname, String username) {
        int score = getGlobalScore(username);

        for (Room r : active_rooms) {
            if (r.getRoom_name().equals(roomname)) {
                r.addPlayer(username, score);
                break;
            }
        }
    }

    /**
     * Entfernt einen Benutzer aus einem Raum.
     *
     * @param roomname der Name des Raums, aus dem der Benutzer entfernt wird
     * @param username der Benutzername des Benutzers, der den Raum verlässt
     */
    public void leaveRoom(String roomname, String username) {
        for (Room r : active_rooms) {
            if (r.getRoom_name().equals(roomname)) {
                r.removePlayer(username);
                break;
            }
        }
    }

    /**
     * Gibt eine Liste der aktiven Räume zurück.
     *
     * @return eine String-Repräsentation der aktiven Räume, getrennt durch '@'
     */
    public String getActiveRooms() {
        StringBuilder active_rooms_string = new StringBuilder();
        for (Room r : active_rooms) {
            active_rooms_string.append(r.toString()).append("@");
        }
        return active_rooms_string.toString();
    }

    /**
     * Gibt eine Liste der aktiven Räume zurück.
     *
     * @return eine String-Repräsentation der aktiven Räume, getrennt durch '@'
     */
    public String reloadRoomInfo(String room_name) {
        StringBuilder users_string = new StringBuilder();
        for (Room r : active_rooms) {
            if (r.getRoom_name().equals(room_name)) {
                return r.toString();
            }
        }

        return users_string.toString();
    }

    /**
     * Entfernt einen SimpleBot aus dem übergebenen Spielraum
     *
     * @param roomname ist der Spielraum, aus dem entfernt wird
     */
    public void removeSimpleBot(String roomname) {
        for (Room r : active_rooms) {
            if (r.getRoom_name().equals(roomname)) {
                String[] tmp = r.toString().split(":");
                String[] players_with_bots = Arrays.copyOfRange(tmp, 2, tmp.length);

                for (String p : players_with_bots) {
                    if (p.startsWith("#")) {
                        r.removePlayer(p.replace("#", ""));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Entfernt einen AdvancedBot aus dem übergebenen Spielraum
     *
     * @param roomname ist der Spielraum, aus dem entfernt wird
     */
    public void removeHardBot(String roomname) {
        for (Room r : active_rooms) {
            if (r.getRoom_name().equals(roomname)) {
                String[] tmp = r.toString().split(":");
                String[] players_with_bots = Arrays.copyOfRange(tmp, 2, tmp.length);

                for (String p : players_with_bots) {
                    if (p.startsWith("*")) {
                        r.removePlayer(p.replace("*", ""));
                        break;
                    }
                }
            }
        }
    }


    // --------------------------------- Game Methoden ---------------------------------
    // Initialization
    /**
     * Startet ein Spiel in einem Raum und entfernt den Raum aus der Liste der aktiven Räume.
     *
     * @param room_name der Name des Raums, in dem das Spiel gestartet wird
     */
    public synchronized void startGame(String room_name) throws InvalidMoveException {
        ArrayList<Player> players = null;
        for (Room r : active_rooms) {
            if (r.getRoom_name().equals(room_name)) {
                active_rooms.remove(r);
                players = r.getPlayers();
                break;
            }
        }

        Table new_table = new Table(room_name, this);

        for (Player p : players) {
            new_table.addPlayer(p);
        }

        active_tables.add(new_table);

        new_table.initializeBots();

        System.out.println("[Server] " + room_name + " wurde erfolgreich gestartet");
    }

    /**
     * Überprüft, ob ein Spiel für den angegebenen Raum bereits gestartet wurde.
     *
     * @param room_name der Name des Raums
     * @return true, wenn das Spiel gestartet wurde, andernfalls false
     */
    public boolean hasGameStarted(String room_name) {
        for (Table t : active_tables) {
            if (t.getTableName().equals(room_name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Entfernt einen Benutzer von einem Tisch.
     *
     * @param table_name der Name des Tisches
     * @param user_name der Benutzername des zu entfernenden Benutzers
     * @throws DeckEmptyException wenn der Deck leer ist
     * @throws InterruptedException wenn der Thread unterbrochen wird
     */
    public void leaveTable(String table_name, String user_name) throws DeckEmptyException, InterruptedException {
        for (Table t : active_tables) {
            if (t.getTableName().equals(table_name)) {
                t.removePlayer(user_name);
                break;
            }
        }
    }

    /**
     * Gibt die Liste der aktiven Tische zurück.
     *
     * @return eine ArrayList der aktiven Tische
     */
    public ArrayList<Table> getActive_tables() {
        return this.active_tables;
    }


    // Ingame
    /**
     * Initialisiert das Spiel für den angegebenen Tisch und Benutzer.
     *
     * @param table_name der Name des Tisches
     * @param user_name der Benutzername des Spielers
     * @return eine String-Repräsentation der Initialisierungsinformationen
     */
    public String initializeGame(String table_name, String user_name) {
        Table table = null;
        for (Table t : active_tables) {
            if (t.getTableName().equals(table_name)) {
                table = t;
            }
        }

        StringBuilder info = new StringBuilder();

        info.append(getGlobalScore(user_name) + "@" + user_name + "@");

        for (int i = table.getPlayers().size() - 1; i >= 0; i--) {
            Player p = table.getPlayers().get(i);
            if (!p.getUsername().equals(user_name)) {
                info.append(p.getUsername() + "@");
            }
        }

        return info.toString();
    }

    /**
     * Gibt die Scores aller Spieler an einem bestimmten Tisch zurück.
     *
     * @param table_name der Name des Tisches
     * @return eine String-Repräsentation der Scores der Spieler
     */
    public String getAllScores(String table_name) {
        StringBuilder scores = new StringBuilder();

        for (Table t : active_tables) {
            if (t.getTableName().equals(table_name)) {
                for (Player p : t.getPlayers()) {
                    scores.append(p.getUsername() + "/" + t.getCardScoreOfPlayer(p.getUsername(), 0) + "/"+ t.getCardScoreOfPlayer(p.getUsername(), 1) + ":");
                }
            }
        }
        return scores.toString();
    }

    /**
     * Setzt den Einsatz eines Benutzers an einem bestimmten Tisch.
     *
     * @param table_name der Name des Tisches
     * @param user_name der Benutzername des Spielers
     * @param stake der Einsatz des Spielers
     * @throws InvalidMoveException wenn der Einsatz ungültig ist
     */
    public synchronized void setStake(String table_name, String user_name, int stake) throws InvalidMoveException {
        for (Table t : active_tables) {
            if (t.getTableName().equals(table_name)) {
                for (Player p : t.getPlayers()) {
                    if (p.getUsername().equals(user_name) && (!t.getActivePlayers().contains(p))) {
                        t.setStake(p, stake);

                        System.out.println("[Server] Einsatz (" + stake + ") von " + user_name + " an Tisch " + table_name + " erfolgreich gesetzt");
                    }
                }
            }
        }
    }

    /**
     * Gibt den aktuellen Spielstatus für einen bestimmten Tisch und Benutzer zurück.
     *
     * @param table_name der Name des Tisches
     * @param user_name der Benutzername des Spielers
     * @return eine String-Repräsentation des Spielstatus
     */
    public String getGameState(String table_name, String user_name) {
        // balance@
        // active_player:ddable:splitable:surrenderable@
        // player_hand_1_card_1:player_hand_1_card_2:...&player_hand_2_card_1:player_hand_2_card_2:...@
        // visibility_card_2&dealer_card_1:dealer_card_2:...@
        // player_1&stake_1:player_2&stake_2:...


        StringBuilder state = new StringBuilder();

        state.append("@");

        for (Table t : active_tables) {
            if (t.getTableName().equals(table_name)) {
                int time_till_start = t.getCountdown();

                if (time_till_start == 0) {
                    if (!t.isGameFinished()) {
                        compressGameStateInfo(user_name, state, t);

                        return state.toString();

                    } else if (!t.alreadyGotResult(user_name)) {
                        t.addResultPlayer(user_name);
                        // Letzte Spielinfos vor Spielende
                        compressGameStateInfo(user_name, state, t);

                        ArrayList<WinningsEntry> result = t.getResults(user_name);

                        if (!(result == null)) {
                            String tmp = result.get(0).getString();
                            int new_balance_after_game_end = Integer.parseInt(tmp.split(":")[0]);
                            setGlobalScore(user_name, new_balance_after_game_end);

                            return ("result#" + getGlobalScore(user_name) + state + "#" + tmp);
                        } else {
                            return ("result#" + state);
                        }
                    }
                } else {
                    return "countdown:" + time_till_start;
                }
            }
        }
        return null;
    }

    /**
     * Komprimiert die Spielstatusinformationen für einen Benutzer und Tisch.
     *
     * @param user_name der Benutzername des Spielers
     * @param state der StringBuilder für den Status
     * @param t der Tisch, dessen Status komprimiert wird
     */
    private void compressGameStateInfo(String user_name, StringBuilder state, Table t) {
        String currentPlayer = t.getCurrentPlayer().getUsername();
        state.append(currentPlayer + ":" + t.isDdAllowed() + ":" + t.isSplitAllowed() + ":" + t.isSurrenderAllowed() + "@");

        ArrayList<ArrayList<Card>> hand = t.getHand(user_name);

        if (hand != null) { // Spieler nimmt aktiv am Spiel teil und hat eine Hand
            for (ArrayList<Card> h : hand) {
                for (Card c : h) {
                    state.append(c.getBildIdent() + ":");
                }
                state.append("&");
            }
        }

        state.append("@" + t.getDealer().getDealerInfo() + "@");
        for (Player p : t.getActivePlayers()) {
            Integer sum = 0;
            for (Integer i : t.getStake(p.getUsername())) {
                sum+=i;
            }

            state.append(p.getUsername() + "&" + sum + ":");
        }
    }

    /**
     * Überprüft, ob das Verdoppeln des Einsatzes an einem bestimmten Tisch erlaubt ist.
     *
     * @param table_name der Name des Tisches
     * @return true, wenn das Verdoppeln erlaubt ist, andernfalls false
     */
    public boolean isDdAllowedBot(String table_name){
        for (Table t : active_tables) {
            if (t.getTableName().equals(table_name)) {
                return t.isDdAllowed();
            }
        }
        return false;
    }

    /**
     * Überprüft, ob das Aufgeben an einem bestimmten Tisch erlaubt ist.
     *
     * @param table_name der Name des Tisches
     * @return true, wenn das Aufgeben erlaubt ist, andernfalls false
     */
    public boolean isSurrenderAllowedBot(String table_name){
        for (Table t : active_tables) {
            if (t.getTableName().equals(table_name)) {
                return t.isSurrenderAllowed();
            }
        }
        return false;
    }

    /**
     * Lässt den aktuellen Spieler an einem bestimmten Tisch eine Karte ziehen.
     *
     * @param table_name der Name des Tisches
     * @throws DeckEmptyException wenn der Deck leer ist
     */
    public synchronized void hit(String table_name) throws DeckEmptyException {
        for (Table t : active_tables) {
            if (t.getTableName().equals(table_name)) {
                System.out.println("[Server] Der Aktive Spieler an Tisch " + table_name + " hat eine Karte gezogen");
                t.hit();
            }
        }
    }

    /**
     * Lässt den aktuellen Spieler an einem bestimmten Tisch stehen bleiben.
     *
     * @param table_name der Name des Tisches
     * @throws DeckEmptyException wenn der Deck leer ist
     */
    public synchronized void stand(String table_name) throws DeckEmptyException {
        for (Table t : active_tables) {
            if (t.getTableName().equals(table_name)) {
                System.out.println("[Server] Der Aktive Spieler an Tisch " + table_name + " zieht keine Karte");
                t.stand();
            }
        }
    }

    /**
     * Lässt den aktuellen Spieler an einem bestimmten Tisch den Einsatz verdoppeln.
     *
     * @param table_name der Name des Tisches
     * @throws DeckEmptyException wenn der Deck leer ist
     */
    public synchronized void doubledown(String table_name) throws DeckEmptyException {
        for (Table t : active_tables) {
            if (t.getTableName().equals(table_name)) {
                System.out.println("[Server] Der Aktive Spieler an Tisch " + table_name + " macht DD");
                t.doubleDown();
            }
        }
    }

    /**
     * Lässt den aktuellen Spieler an einem bestimmten Tisch seine Karten splitten.
     *
     * @param table_name der Name des Tisches
     * @throws DeckEmptyException wenn der Deck leer ist
     */
    public synchronized void split(String table_name) throws DeckEmptyException {
        for (Table t : active_tables) {
            if (t.getTableName().equals(table_name)) {
                System.out.println("[Server] Der Aktive Spieler an Tisch " + table_name + " splittet seine Karten");
                t.split();
            }
        }
    }

    /**
     * Lässt den aktuellen Spieler an einem bestimmten Tisch aufgeben.
     *
     * @param table_name der Name des Tisches
     * @throws DeckEmptyException wenn der Deck leer ist
     */
    public synchronized void surrender(String table_name) throws DeckEmptyException {
        for (Table t : active_tables) {
            if (t.getTableName().equals(table_name)) {
                System.out.println("[Server] Der Aktive Spieler an Tisch " + table_name + " gibt auf");
                t.surrender();
            }
        }
    }


    // --------------------------------- Scoreboard Methoden ---------------------------------

    /**
     * Gibt das aktuelle Scoreboard zurück.
     *
     * @return eine String-Repräsentation des Scoreboards, getrennt durch '@'
     */
    public String getScoreboard() {
        StringBuilder scoreboard_string = new StringBuilder();
        for (Score s : scoreboard) {
            scoreboard_string.append(s.getUser()).append(":").append(s.getScore()).append("@");
        }
        return scoreboard_string.toString();
    }

    /**
     * Gibt den globalen Score eines Benutzers zurück.
     *
     * @param user_name der Benutzername des Benutzers, dessen Score abgerufen wird
     * @return der Score des Benutzers
     */
    public int getGlobalScore(String user_name) {
        loadScoreboardFromFile();

        for (Score s : scoreboard) {
            if (s.getUser().equals(user_name)) {
                return s.getScore();
            }
        }
        return 0;
    }

    /**
     * Setzt den globalen Score eines Benutzers.
     *
     * @param user_name der Benutzername des Benutzers
     * @param new_score der neue Score des Benutzers
     */
    public synchronized void setGlobalScore(String user_name, int new_score) {
        loadScoreboardFromFile();

        for (Score s : scoreboard) {
            if (s.getUser().equals(user_name)) {
                s.setScore(new_score);
            }
        }

        saveScoreboardToFile();
    }

    // --------------------------------- Chat Methoden ---------------------------------

    /**
     * Speichert eine Chat-Nachricht.
     *
     * @param user der Benutzername des Benutzers, der die Nachricht sendet
     * @param message der Inhalt der Nachricht
     * @throws IllegalArgumentException wenn der Benutzername oder die Nachricht null oder leer ist
     */
    public synchronized void saveChatMessage(String user, String message) {
        if (user == null || user.isEmpty() || message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Benutzername und Nachricht dürfen nicht null oder leer sein.");
        }
        chat.add(user + ": " + message);
    }

    /**
     * Gibt alle aktuellen Chat-Nachrichten zurück.
     *
     * @return eine ArrayList der Chat-Nachrichten
     */
    public ArrayList<String> getChatMessages() {
        return chat;
    }

    // --------------------------------- Laden und Speichern der Datenbank ---------------------------------

    /**
     * Lädt Benutzer aus login.txt in die Benutzerliste.
     *
     * @throws IOException wenn ein I/O-Fehler beim Lesen der Datei auftritt
     */
    private synchronized void loadUsersFromFile() {
        String tempLoginFilePath = jarDir + "/temp_login.txt";

        File tempLoginFile = new File(tempLoginFilePath);

        try (BufferedReader br = new BufferedReader(new FileReader(tempLoginFile))) {
            loadUsers(br);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUsers(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(":");
            String username = parts[0];
            String password = parts[1];

            boolean dup = false;
            for (User u : users) {
                if (u.getUsername().equals(username)) {
                    dup = true;
                    break;
                }
            }
            if (!dup) {
                User temp_user = new User(username, password);
                users.add(temp_user);
            }
        }
    }

    /**
     * Lädt das Scoreboard aus scoreboard.txt in die Scoreboard-Liste.
     *
     * @throws IOException wenn ein I/O-Fehler beim Lesen der Datei auftritt
     */
    private synchronized void loadScoreboardFromFile() {
        String tempScoreboardFilePath = jarDir + "/temp_scoreboard.txt";

        File tempScoreboardFile = new File(tempScoreboardFilePath);

        try (BufferedReader br = new BufferedReader(new FileReader(tempScoreboardFile))) {
            loadScores(br);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadScores(BufferedReader br) throws IOException {
        ArrayList<Score> tmp_scoreboard = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(":");
            String username = parts[0];
            String score = parts[1];

            Score tmp_score = new Score(username, Integer.parseInt(score));
            tmp_scoreboard.add(tmp_score);
        }

        // Sortiere Scoreboard
        tmp_scoreboard.sort((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()));

        if (!tmp_scoreboard.equals(scoreboard)) {
            scoreboard = tmp_scoreboard;
        }
    }

    /**
     * Speichert die Benutzer (Logindaten) in der login.txt.
     *
     * @throws IOException wenn ein I/O-Fehler beim Schreiben in die Datei auftritt
     */
    private synchronized void saveUsersToFile() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(jarDir + "/temp_login.txt"))) {
            for (User user : users) {
                String line = user.getUsername() + ":" + user.getPassword();
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    /**
     * Speichert das Scoreboard in der scoreboard.txt.
     *
     * @throws IOException wenn ein I/O-Fehler beim Schreiben in die Datei auftritt
     */
    private synchronized void saveScoreboardToFile() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(jarDir + "/temp_scoreboard.txt"))) {
            for (Score score : scoreboard) {
                String line = score.getUser() + ":" + score.getScore();
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

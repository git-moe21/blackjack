package com.example.blackjack.utils.game;

import com.example.blackjack.utils.game.player.Player;
import com.example.blackjack.utils.game.player.bots.AdvancedBot;
import com.example.blackjack.utils.game.player.bots.SimpleBot;
import com.example.blackjack.utils.server.Database;

import java.util.ArrayList;
import java.util.Random;

/**
 * Die Room-Klasse repräsentiert einen Spielraum im Blackjack-Spiel.
 * Jeder Raum enthält eine Liste von Spielern und einen Tisch.
 */
public class Room {
    private String room_name; // Der Name des Raums
    private ArrayList<Player> players; // Die Liste der Spieler im Raum
    private Database database;

    /**
     * Konstruktor für Room.
     *
     * @param name Der Name des Raums
     * @param simple_bot_count Die Anzahl der einfachen Bots
     * @param advanced_bot_count Die Anzahl der fortgeschrittenen Bots
     */
    public Room(String name, int simple_bot_count, int advanced_bot_count, Database database) {
        this.room_name = name;
        this.players = new ArrayList<>();
        this.database = database;

        initializeBots(simple_bot_count, advanced_bot_count);
    }

    /**
     * Generiert einen eindeutigen Namen für einen Bot.
     *
     * @return Ein eindeutiger Name für den Bot
     */
    private String generateUniqueName() {
        String[] words = {"Ace", "Blaze", "Cobra", "Dynamo", "Echo", "Falcon", "Ghost", "Hawk", "Inferno", "Jaguar", "Knight", "Lynx", "Maverick", "Nitro", "Omega", "Phantom", "Quasar", "Raven", "Shadow", "Tiger", "Ultra", "Viper", "Wolf", "Xenon", "Zephyr"};
        Random random = new Random();
        String word = words[random.nextInt(words.length)];
        int number = random.nextInt(10000);
        return word + number;
    }

    /**
     * Initialisiert die Bots im Raum.
     *
     * @param simple Die Anzahl der einfachen Bots
     * @param advanced Die Anzahl der fortgeschrittenen Bots
     */
    private void initializeBots(int simple, int advanced) {
        for (int i = 1; i <= simple; i++) { // Einfache Bots
            players.add(new SimpleBot(generateUniqueName(), 1000, database, room_name));
        }

        for (int i = 1; i <= advanced; i++) { // Fortgeschrittene Bots
            players.add(new AdvancedBot(generateUniqueName(), 1000, database, room_name));
        }
    }

    /**
     * Gibt die Liste der Spieler im Raum zurück.
     *
     * @return Die Liste der Spieler
     */
    public ArrayList<Player> getPlayers() {
        return players;
    }

    /**
     * Gibt den Namen des Raums zurück.
     *
     * @return Der Name des Raums
     */
    public String getRoom_name() {
        return room_name;
    }

    /**
     * Fügt einen Spieler zum Raum hinzu.
     *
     * @param username Der Benutzername des Spielers
     * @param wealth Das Vermögen des Spielers
     */
    public void addPlayer(String username, int wealth) {
        if (players.size() < 8) {
            for (Player p : players) {
                if (p.getUsername().equals(username)) {
                    return;
                }
            }
            players.add(new Player(username, wealth));
        } else {
            System.out.println("[Server] Raum ist voll");
        }
    }

    /**
     * Entfernt einen Spieler aus dem Raum.
     *
     * @param player Der Benutzername des zu entfernenden Spielers
     */
    public void removePlayer(String player) {
        for (Player p : players) {
            if (p.getUsername().equals(player)) {
                players.remove(p);
                break;
            }
        }
    }

    /**
     * Gibt die Anzahl der Spieler im Raum zurück.
     *
     * @return Die Anzahl der Spieler
     */
    public int getPlayerCount() {
        return players.size();
    }


    /**
     * Gibt eine String-Repräsentation des Raums zurück.
     *
     * @return Eine String-Repräsentation des Raums
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(room_name + ":").append(getPlayerCount());

        for (int i = players.size() - 1; i >= 0; i--) {
            Player p = players.get(i);
            if (p.getClass().equals(SimpleBot.class)) {
                sb.append(":" + "#" + p.getUsername());
            } else if (p.getClass().equals(AdvancedBot.class)) {
                sb.append(":" + "*" + p.getUsername());
            } else {
                sb.append(":" + p.getUsername());
            }
        }

        return sb.toString();
    }
}

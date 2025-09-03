package com.example.blackjack.utils.game.player;

import com.example.blackjack.utils.game.exceptions.InvalidMoveException;

/**
 * Die Player-Klasse repräsentiert einen Spieler im Blackjack-Spiel.
 */
public class Player {
    private final String username; // Der Benutzername des Spielers
    private int wealth; // Das Vermögen des Spielers

    /**
     * Konstruktor für Player.
     *
     * @param username Der Benutzername des Spielers
     * @param wealth Das anfängliche Vermögen des Spielers
     */
    public Player(String username, int wealth) {
        this.username = username;
        this.wealth = wealth;
    }

    /**
     * Bots verwenden diese Funktion, brauchen aber die Funktion nur für einen Error
     */
    public void init() throws InvalidMoveException {
        // Nur für Bots
    }

    /**
     * Gibt den Benutzernamen des Spielers zurück.
     *
     * @return Der Benutzername des Spielers
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gibt das Vermögen des Spielers zurück.
     *
     * @return Das Vermögen des Spielers
     */
    public int getWealth() {
        return wealth;
    }


    /**
     * Erhöht das Vermögen des Spielers um den angegebenen Betrag.
     *
     * @param amount Der Betrag, um den das Vermögen erhöht wird
     */
    public void addWealth(int amount) {
        wealth += amount;
    }

    /**
     * Setzt das Vermögen des Spielers.
     *
     * @param wealth Das neue Vermögen des Spielers
     */
    public void setWealth(int wealth) {
        this.wealth = wealth;
    }
}

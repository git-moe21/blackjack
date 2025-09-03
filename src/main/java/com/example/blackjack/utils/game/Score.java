package com.example.blackjack.utils.game;

/**
 * Die Score-Klasse repräsentiert die Punktzahl eines Benutzers im Blackjack-Spiel.
 */
public class Score {
    private String user; // Der Benutzername
    private int score; // Die Punktzahl

    /**
     * Konstruktor für Score.
     *
     * @param user Der Benutzername
     * @param score Die Punktzahl des Benutzers
     */
    public Score(String user, int score) {
        this.user = user;
        this.score = score;
    }

    /**
     * Gibt den Benutzernamen zurück.
     *
     * @return Der Benutzername
     */
    public String getUser() {
        return user;
    }

    /**
     * Gibt die Punktzahl des Benutzers zurück.
     *
     * @return Die Punktzahl des Benutzers
     */
    public int getScore() {
        return score;
    }

    /**
     * Setzt die Punktzahl des Benutzers.
     *
     * @param score Die neue Punktzahl des Benutzers
     */
    public void setScore(int score) {
        this.score = score;
    }
}

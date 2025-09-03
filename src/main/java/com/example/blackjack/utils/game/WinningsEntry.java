package com.example.blackjack.utils.game;

import com.example.blackjack.utils.game.player.Player;

/**
 * Die Klasse wird als Entry verwendet in der winning-Liste in Table, um zum schluss die Gewinne berechnen zu können.
 */
public class WinningsEntry {
    private Player player;
    private final int stake;
    private final int score;
    private final boolean blackjack;

    /**
     * Initialisiert den Eintrag
     *
     * @param winning ist die Höhe des Gewinns bei Gewinn
     * @param score ist der Kartenscore
     * @param blackjack ist ein Wahrheitswert, ob der Spieler ein Blackjack hat
     */
    public WinningsEntry(int winning, int score, boolean blackjack) {
        this.stake = winning;
        this.score = score; // wenn score = 0, wurde surrender verwendet
        this.blackjack = blackjack;
    }

    /**
     * Der Gewinn den der Spieler erhalten würde bei Gewinn.
     *
     * @return Höhe des Gewinns
     */
    public int getStake() {
        return stake;
    }

    /**
     * Karten-Score der Hand
     *
     * @return Score der aktuellen Hand
     */
    public int getScore() {
        return score;
    }


    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Wahrheitswert, ob der Spieler ein Blackjack hat
     *
     * @return Wahrheitswert, bei Blackjack true
     */
    public boolean isBlackjack() {
        return blackjack;
    }

    public String getString() {
        return (player.getWealth() + ":" + blackjack + ":" + score + ":" + stake);
    }

}

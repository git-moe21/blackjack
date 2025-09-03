package com.example.blackjack.utils.game;

import com.example.blackjack.utils.game.exceptions.DeckEmptyException;
import com.example.blackjack.utils.game.player.Player;

import java.util.ArrayList;

/**
 * Der Dealer ist dazu da, seine Hand zu managen und Karten nach einer Runde zurück in den Kartenstapel zu legen.
 * Außerdem muss er den Rundenbeginn starten, indem er die Karten verteilt.
 */
public class Dealer {
    private final ArrayList<Card> hand;
    private final Deck deck;
    private final Table table;
    private boolean secondCardVisibility;
    private boolean blackjack = false;
    private int score = 0;

    /**
     * Initialisieren des Dealer-Objekts
     *
     * @param deck Das Spieldeck, auf das im Spielverlauf zugegriffen wird.
     * @param table Das Table-Objekt auf dem das Spiel läuft.
     */
    public Dealer(Deck deck, Table table) {
        this.hand = new ArrayList<>();
        this.table = table;
        this.deck = deck;
    }

    /**
     * Gibt die aktuellen Informationen des Dealers zurück, einschließlich der Sichtbarkeit der zweiten Karte und der Handkarten des Dealers.
     *
     * @return Ein String, der die Sichtbarkeit der zweiten Karte und die Kartenbilder des Dealers beschreibt.
     */
    public String getDealerInfo() {
        StringBuilder state = new StringBuilder();

        state.append(secondCardVisibility + "&");
        for (Card c : hand) {
            state.append(c.getBildIdent() + ":");
        }

        return state.toString();
    }

    /**
     * Beginnt die Runde.
     * Dabei erhällt jeder Spieler und der Dealer eine offene Karte.
     * Anschließend erhält jeder Spieler eine weitere offene Karte und der Dealer eine verdeckte.
     *
     * @param activePlayers Die Liste aller aktiven (haben einen Einsatz gesetzt) Spieler
     * @throws DeckEmptyException wenn das Deck leer sein sollte.
     */
    public void beginRound(ArrayList<Player> activePlayers) throws DeckEmptyException {
        deck.shuffle();

        for (Player player: activePlayers) {
            table.handCard(deck.dealCard(), player);
        }
        hand.add(deck.dealCard());

        for (Player player: activePlayers) {
            table.handCard(deck.dealCard(), player);
        }

        hand.add(deck.dealCard());
        secondCardVisibility = false;
    }

    /**
     * Lässt den Dealer nach den Regeln Karten ziehen, falls notwendig
     *
     * @throws DeckEmptyException falls das Deck leer sein sollte.
     */
    public void makeMove() throws DeckEmptyException {
        secondCardVisibility = true; //decke 2. Karte auf

        if (checkBlackjack()) {
            this.blackjack = checkBlackjack();
            this.score = 21;
        } else {
            int score = 0;
            int anzAss = 0;

            for (int i = 0; i <= hand.size() - 1; i++) {
                int rank = hand.get(i).getRank();
                if (rank == 1) {
                    score += 11;
                    anzAss++;
                } else if (rank >= 2 && rank <= 9) {
                    score += rank;
                } else if (rank >= 10) {
                    score += 10;
                }
            }
            while (score > 21 && anzAss > 0) {
                score -= 10;
                anzAss--;
            }
            if (score <= 16) {
                hand.add(deck.dealCard());
                makeMove();
            }

            this.score = score;
        }
    }

    /**
     * prüft, ob der aktuelle Spieler ein Blackjack hat
     *
     * @return Wahrheitswert, ob der Spieler ein Blackjack hat.
     */
    public boolean checkBlackjack(){
        return (hand.get(0).getRank() == 1 && hand.get(1).getRank() >= 10) | (hand.get(1).getRank() == 1 && hand.get(0).getRank() >= 10); // true wenn Blackjack
    }

    /**
     * Gebe den Score zurück
     *
     * @return den Score des Dealers
     */
    public int getScore() {
        int score = 0;
        int anzAss = 0;
        for(int i = 0; i <= hand.size() - 1; i++){
            int rank = hand.get(i).getRank();
            if(rank == 1){
                score += 11;
                anzAss++;
            }else if(rank >= 2 && rank <= 9){
                score += rank;
            }else if(rank >= 10){
                score += 10;
            }
        }
        while(score > 21 && anzAss > 0){
            score -= 10;
            anzAss--;
        }
        this.score = score;
        return score;
    }

    /**
     * Gibt die Hand des Dealers zurück, die die Karten des Dealers enthält.
     *
     * @return Eine Liste von Karten, die die Hand des Dealers repräsentieren.
     */
    public ArrayList<Card> getHand() {
        return hand;
    }

    /**
     * Hat der Dealer ein Blackjack?
     *
     * @return true, bei Blackjack
     */
    public boolean getBlackjack() {
        return blackjack;
    }
}

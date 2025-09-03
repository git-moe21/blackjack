package com.example.blackjack.utils.game;

import com.example.blackjack.utils.game.exceptions.DeckEmptyException;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Erstellen des Spieldecks und dessen Karten.
 * Das Deck kann gemischt werden, sowie die oberste Karte ausgeben.
 * Man kann Karten auch wieder hinzufügen.
 */
public class Deck {

    private final ArrayList<Card> cards;

    /**
     * Initialisiere das Spieldeck. Erstellt die Karten.
     */
    public Deck() {
        cards = new ArrayList<>(312);
        for (int i = 0; i < 6; i++) { // 6 Decks (0-5)
            for (int j = 0; j < 4; j++) { // 4 Symbols (0:Hearts, 1:Diamonds, 2:Spades, 3:Clubs)
                for (int k = 1; k <= 13; k++) { //13 Ranks (1:Ace, 2-10:Numbers, 11:Jack, 12:Queen, 13:King)
                    cards.add(new Card(i, k, j));
                }
            }
        }
    }

    /**
     * Mischt die Karten neu.
     */
    public void shuffle(){
        Collections.shuffle(cards);
    }

    /**
     * Gibt die Größe des Kartenstapels zurück.
     *
     * @return Die Größe des Decks
     */
    public int getSize(){
        return cards.size();
    }

    /**
     * Fügt dem Deck die übergebene Karte hinzu. Es sollte anschließen auch gemischt werden
     *
     * @see Deck#shuffle()
     * @param card Die Karte die dem Deck hinzugefügt werden soll.
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    /**
     * Gibt die oberste Karte des Decks zurück und entfernt diese aus dem Deck.
     *
     * @throws DeckEmptyException wenn das Deck leer sein sollte.
     */
    public Card dealCard() throws DeckEmptyException {
        if (!cards.isEmpty()) {
            return cards.remove(0);
        }else{
            throw new DeckEmptyException("Es befinden sich keine Karten im Deck!");
        }
    }
}

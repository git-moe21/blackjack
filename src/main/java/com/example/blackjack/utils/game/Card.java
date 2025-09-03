package com.example.blackjack.utils.game;


/**
 * Dient der Darstellung einer einzigen Karte.
 * Diese besteht aus der deckId: (Es gibt 6 Decks die in einem vermischt werden: DeckID = [0...5]), 
 * dem Rang: 13 Ranks (1:Ace, 2-10:Numbers, 11:Jack, 12:Queen, 13:King),
 * und dem Symbol: 4 Symbols (0:Hearts, 1:Diamonds, 2:Spades, 3:Clubs).
 */
public class Card {

    private final int deckId;
    private final int rank; //13 Ranks (1:Ace, 2-10:Numbers, 11:Jack, 12:Queen, 13:King)
    private final int symbol; // 4 Symbols (0:Hearts, 1:Diamonds, 2:Spades, 3:Clubs)
    private final int bildIdent; // wird verwendet, um das richtige Kartenbild zu identifizieren

    /**
     * Initialisieren einer Karte.
     * 
     * @param deckId die ID des Decks: (Es gibt 6 Decks die in einem vermischt werden: DeckID = [0...5]).
     * @param rank der Rang der Karte: 13 Ranks (1:Ace, 2-10:Numbers, 11:Jack, 12:Queen, 13:King).
     * @param symbol das Symbol der Karte: 4 Symbols (0:Hearts, 1:Diamonds, 2:Spades, 3:Clubs).
     */
    public Card(int deckId, int rank, int symbol) {
        this.deckId = deckId;
        this.rank = rank;
        this.symbol = symbol;
        this.bildIdent = symbol * 13 + rank;
    }

    /**
     * Gibt die DeckID der Karte zurück.
     * (Es gibt 6 Decks die in einem vermischt werden: DeckID = [0...5])
     * 
     * @return Den Integer der DeckID.
     */
    public int getDeckId() {
        return deckId;
    }

    /**
     * Gibt den Rang der Karte zurück.
     * 13 Ranks (1:Ace, 2-10:Numbers, 11:Jack, 12:Queen, 13:King)
     *
     * @return Den Integer des Rangs.
     */
    public int getRank() {
        return rank;
    }

    /**
     * Gibt das Symbol der Karte zurück.
     * 4 Symbols (0:Hearts, 1:Diamonds, 2:Spades, 3:Clubs)
     *
     * @return Den Integer des Symbols.
     */
    public int getSymbol() {
        return symbol;
    }

    /**
     * Gibt die Identifikation des Kartenbildes zurück
     *
     * @return die ID des Bildes (Name der Datei)
     */
    public int getBildIdent() {
        return bildIdent;
    }
}



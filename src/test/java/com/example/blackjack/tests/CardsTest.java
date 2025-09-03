package com.example.blackjack.tests;

import com.example.blackjack.utils.game.*;
import com.example.blackjack.utils.game.exceptions.DeckEmptyException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CardsTest {

    @Test
    public void testCard() {
        Card card1 = new Card(1, 3, 1); // = Karo 3
        Card card2 = new Card(1, 1, 3); // = Kreuz Ass
        Card card3 = new Card(1, 12, 2); // = Schippe Dame

        Card card12 = new Card(2, 3, 1); // = Karo 3 aber anderes Deck

        assertEquals(card1.getDeckId(), 1);
        assertEquals(card2.getRank(), 1);
        assertEquals(card3.getSymbol(), 2);

        assertEquals(card1.getRank(), card12.getRank());
        assertEquals(card1.getSymbol(), card12.getSymbol());

        assertNotEquals(card1.getDeckId(), card12.getDeckId());

        assertEquals(card1.getBildIdent(), 16);
        assertEquals(card2.getBildIdent(), 40);
        assertEquals(card3.getBildIdent(), 38);
    }

    @Test
    public void testDeck() throws DeckEmptyException {
        Deck deck = new Deck();
        int size = deck.getSize();

        Card card1 = deck.dealCard();
        assertEquals(deck.getSize(), size - 1);
        Card card2 = deck.dealCard();
        assertEquals(deck.getSize(), size - 2);
        Card card3 = deck.dealCard();
        assertEquals(deck.getSize(), size - 3);
        Card card4 = deck.dealCard();
        assertEquals(deck.getSize(), size - 4);
        Card card5 = deck.dealCard();
        assertEquals(deck.getSize(), size - 5);

        assertNotEquals(card1, card2);
        assertNotEquals(card2, card3);
        assertNotEquals(card3, card4);
        assertNotEquals(card4, card5);

        size = deck.getSize();
        for (int i = 0; i <= size; i++) {
            Card card = deck.dealCard();
            assertNotEquals(card, card1);
            deck.addCard(card);
        }

        deck.addCard(card1);
        boolean testerFound = false; // schaue ob Karten korrekt in den Stapel hinzugefügt werden
        boolean testerLocation = false;
        size = deck.getSize();
        for (int i = 0; i < size; i++) {
            Card cardX = deck.dealCard();
            if (cardX.getRank() == card1.getRank() && cardX.getSymbol() == card1.getSymbol() && cardX.getDeckId() == card1.getDeckId()) {
                assertEquals(cardX, card1);
                testerFound = true;
            } else {
                assertNotEquals(cardX, card1);
            }
        }

        assertTrue(testerFound);

        deck.addCard(card2); // schaue, ob korrekt gemischt wird
        deck.shuffle();
        int helpLocation = 0;

        size = deck.getSize();
        for (int i = 0; i < size; i++) {
            Card cardY = deck.dealCard();
            if (cardY.getRank() == card2.getRank() && cardY.getSymbol() == card2.getSymbol() && cardY.getDeckId() == card2.getDeckId()) {
                assertEquals(cardY, card2);
                helpLocation = i;
            }
            deck.addCard(cardY);
        }
        boolean shuffled = false;
        for (int i = 0; i < size; i++) {
            Card cardY = deck.dealCard();
            if (cardY.getRank() == card2.getRank() && cardY.getSymbol() == card2.getSymbol() && cardY.getDeckId() == card2.getDeckId()) {
                assertEquals(cardY, card2);
                if (i == helpLocation) {
                    shuffled = true;
                }
            }
        }

        assertTrue(shuffled);// Deck sollte nun leer sein

        assertThrows(DeckEmptyException.class, deck::dealCard);
    }


    /*
    @Test
    public void testDealerTable() throws DeckEmptyException {
        Table table = new Table(0);
        Room room = new Room("Test Raum");

        Player p1 = new Player("player1", 100, table);
        Player p2 = new Player("player2", 200, table);
        Player p3 = new Player("player3", 300, table);
        table.addPlayer(p1);
        table.addPlayer(p2);
        table.addPlayer(p3);
        p1.setStake(5);
        p2.setStake(50);

        table.checkStakes(); // überprüft, welche spieler stakes gesetzt haben
        int size = table.getDeck().getSize();
        table.beginRound();
        assertEquals(table.getDeck().getSize(), size - 6); // 2 Spieler + Dealer mit je 2 Karten
        table.evaluateGame();

        for(int i = 0; i <= table.getDeck().getSize(); i++){ // um das Deck zu leeren
            table.getDeck().addCard(table.getDeck().dealCard());
        }
        Card c1 = new Card(1, 1, 1);
        Card c2 = new Card(2, 2, 2);
        table.getDeck().addCard(c1);
        table.getDeck().addCard(c2);

        table.getDealer().drawCard();
        table.getDealer().drawCard();

        assertNotEquals(table.getDealer().getHand(), null);
        assertEquals(table.getDealer().getHand().size(), 2);
        assertEquals(table.getDealer().getHand().get(0), c1);
        assertEquals(table.getDealer().getHand().get(1), c2);

        table.getDeck().addCard(new Card(1, 1, 1)); // Karo Ass
        table.getDeck().addCard(new Card(2, 1, 1));
        table.getDeck().addCard(new Card(3, 1, 1));
        table.getDeck().addCard(new Card(1, 10, 1)); // Karo 10
        table.getDeck().addCard(new Card(2, 10, 1));
        table.getDeck().addCard(new Card(1, 7, 1)); // Karo 7
        // nun sollten beide Spieler ein Blackjack haben und der Dealer 17

        assertTrue(table.checkBlackjack());

        table.checkStakes();
        table.beginRound();
        table.evaluateGame();
    }
    */
}

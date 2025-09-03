package com.example.blackjack.tests;

import com.example.blackjack.utils.game.Dealer;
import com.example.blackjack.utils.game.Table;
import com.example.blackjack.utils.game.exceptions.DeckEmptyException;
import com.example.blackjack.utils.game.Card;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DealerTest {

    private Table table;
    private Card card1, card2, card3;
    private Dealer dealer;

    @Before
    public void setUp() {
        table = new Table("test", null);
        dealer = table.getDealer();
        card1 = new Card(1, 10, 0); // Herz 10
        card2 = new Card(1, 1, 0); // Herz Ass
        card3 = new Card(1,5,0); // Herz 5
    }

    @Test
    public void testGetHand() {
        dealer.getHand().add(0, card1);
        dealer.getHand().add(1, card2);
        assertEquals(21, dealer.getScore());
        assertTrue(dealer.checkBlackjack());
        assertEquals("false&10:1:", dealer.getDealerInfo());
    }

    @Test
    public void testInfo() {
        dealer.getHand().add(0, card1);
        dealer.getHand().add(1, card2);
        assertEquals("false&10:1:", dealer.getDealerInfo());
    }

    @Test
    public void testMove() throws DeckEmptyException {
        dealer.getHand().add(0, card1);
        dealer.getHand().add(1, card2);
        dealer.makeMove();
        assertEquals(21, dealer.getScore());
    }

    @Test
    public void testBlackjack1() {
        dealer.getHand().add(0, card1);
        dealer.getHand().add(1, card1);
        assertEquals(20, dealer.getScore());
        assertFalse(dealer.checkBlackjack());
    }

    @Test
    public void testBlackjack2() {
        dealer.getHand().add(0, card3);
        dealer.getHand().add(1, card3);
        assertEquals(10, dealer.getScore());
        assertFalse(dealer.checkBlackjack());
    }
}
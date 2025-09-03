package com.example.blackjack.tests;

import com.example.blackjack.utils.game.Deck;
import com.example.blackjack.utils.game.Table;
import com.example.blackjack.utils.game.WinningsEntry;
import com.example.blackjack.utils.game.exceptions.DeckEmptyException;
import com.example.blackjack.utils.game.exceptions.InvalidMoveException;
import com.example.blackjack.utils.game.player.Player;
import com.example.blackjack.utils.game.Card;
import com.example.blackjack.utils.game.player.bots.AdvancedBot;
import com.example.blackjack.utils.game.player.bots.SimpleBot;
import com.example.blackjack.utils.server.Database;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class TableTest {

    private Table table;
    private Player player;
    private Card card;
    private Database database;

    @Before
    public void setUp() {
        database = new Database();
        table = new Table("test", database);
        player = new Player("testPlayer", 1000);
        card = new Card(1, 1,0); // Herz Ass
        table.setCurPlayer(player);
    }

    @Test
    public void testGetId() {
        String name = table.getTableName();
        assertEquals("test", name);
    }

    @Test
    public void testGetDealer() {
        assertNotNull(table.getDealer());
    }

    @Test
    public void testGetActivePlayer() throws InvalidMoveException {
        table.initializeBots();
        table.addPlayer(player);
        table.setStake(player, 100);
        ArrayList<Player> players = new ArrayList<>();
        players.add(player);
        assertEquals(players, table.getActivePlayers());
    }

    @Test
    public void testGetCardScorePlayer() throws InvalidMoveException, DeckEmptyException {
        table.addPlayer(player);
        table.setStake(player, 100);
        table.handCard(new Card(1,10,0), player);
        table.handCard(new Card(1,12,0), player);
        assertEquals(20, table.getCardScoreOfPlayer(player.getUsername(), 0));
        table.split();
        Card c = table.getHand(player.getUsername()).get(1).get(1);
        int score = 0;
        if(c.getRank() == 1){
            score = 11;
        }else if(c.getRank() >= 10){
            score = 10;
        }else{
            score = c.getRank();
        }
        assertEquals(score+10, table.getCardScoreOfPlayer(player.getUsername(), 1));
    }

    @Test
    public void testCountdown() {
        int countDownOld = table.getCountdown();
        assertEquals(countDownOld, table.getCountdown());
    }

    @Test
    public void testResultPlayer() {
        table.addPlayer(player);
        table.addResultPlayer(player.getUsername());
        table.addResultPlayer("tester2");
        table.addResultPlayer("tester2");
        assertTrue(table.alreadyGotResult(player.getUsername()));
        assertTrue(table.alreadyGotResult("tester2"));
    }

    @Test
    public void testWinningsReturn(){
        WinningsEntry wE = new WinningsEntry(100,0,true);
        wE.setPlayer(player);
        assertEquals(wE.getPlayer(), player);
        assertEquals(wE.getString(), player.getWealth() + ":true:0:100");
    }

    @Test
    public void testHandCard() throws InvalidMoveException {
        table.addPlayer(player);
        table.setStake(player, 100); // Spieler in activePlayers machen
        table.handCard(card, player);
        assertEquals(1, table.getHand(player.getUsername()).get(0).size());
    }

    @Test
    public void testAddAndRemovePlayer1() throws DeckEmptyException, InvalidMoveException, InterruptedException {
        table.removePlayer("testFalse"); // es sollte nichts passieren
        table.addPlayer(player);
        assertTrue(table.getPlayers().contains(player));
        table.removePlayer(player.getUsername());
        assertTrue(table.getPlayers().isEmpty());
        table.addPlayer(player);
        table.setStake(player, 100);
        table.getDealer().getHand().add(new Card(1, 1,0));
        table.getDealer().getHand().add(new Card(1, 1,0));// Dealer hat Karten, dass kein Fehler auftritt
        table.removePlayer(player.getUsername());
    }

    @Test
    public void testAddAndRemovePlayer2() throws DeckEmptyException, InvalidMoveException, InterruptedException {
        table.addPlayer(player);
        Player p2 = new Player("testPlayer2", 1000);
        table.addPlayer(p2);
        assertTrue(table.getPlayers().contains(player) && table.getPlayers().contains(p2));
        table.setStake(player, 100);
        table.setStake(p2, 100);
        table.getDealer().getHand().add(new Card(1, 1,0));
        table.getDealer().getHand().add(new Card(1, 1,0));// Dealer hat Karten, dass kein Fehler auftritt
        table.removePlayer(p2.getUsername());
        assertFalse(table.getPlayers().contains(p2));
    }

    @Test
    public void testBotGame1() throws DeckEmptyException, InterruptedException, InvalidMoveException {
        table.addPlayer(new SimpleBot("athie", 1000, null, "test_table"));
        table.addPlayer(player);
        assertFalse(table.isBotGame());
        table.removePlayer(player.getUsername());
        assertTrue(table.isBotGame());
    }

    @Test
    public void testBotGame2() throws DeckEmptyException, InterruptedException {
        table.addPlayer(new AdvancedBot("athie", 1000, null, "test_table"));
        table.addPlayer(player);
        assertFalse(table.isBotGame());
        table.removePlayer(player.getUsername());
        assertTrue(table.isBotGame());
    }


    @Test
    public void testBeginRound() throws DeckEmptyException, InvalidMoveException, InterruptedException {
        table.addPlayer(player);
        assertThrows(InvalidMoveException.class, () -> { table.setStake(player, 2000);});
        table.setStake(player, 100);
        table.beginRound();
        assertFalse(table.getHand(player.getUsername()).get(0).isEmpty());
    }

    @Test(expected = InvalidMoveException.class)
    public void testSetStakeInvalidMoveException() throws InvalidMoveException {
        player = new Player("testPlayer", 50); // Spieler mit zu wenig Geld
        table.setStake(player, 100);
    }

    @Test
    public void testSetStake() throws InvalidMoveException {
        table.setStake(player, 100);
        assertTrue(table.getStake(player.getUsername()).contains(100));
    }

    @Test
    public void testCheckBlackjack() throws InvalidMoveException {
        table.addPlayer(player);
        table.setStake(player, 100); // Spieler in activePlayers machen
        ArrayList<Card> playerCards = new ArrayList<>();
        playerCards.add(new Card(1, 1, 0)); // Herz Ass
        playerCards.add(new Card(1, 10, 0)); // Herz 10
        table.getHand(player.getUsername()).set(0, playerCards);
        assertTrue(table.checkBlackjack());
    }

    @Test
    public void testMove() throws InvalidMoveException, DeckEmptyException{
        Table t = new Table("sgh", database);
        t.addPlayer(player);
        Player p2 = new Player("testPlayer2", 1000);
        t.setStake(player, 100);
        t.setStake(p2, 100);
        t.setStake(p2, 100);
        t.beginRound();
        ArrayList<Card> playerCards = new ArrayList<>();
        playerCards.add(new Card(1, 10, 1)); // KAro 10
        playerCards.add(new Card(1, 10, 0)); // Herz 10
        t.getHand(player.getUsername()).set(0, playerCards);
        t.split();
        t.getHand(player.getUsername()).get(0).set(1, new Card(1, 1, 1));
        t.getHand(player.getUsername()).get(1).set(1, new Card(1, 5, 1));
        t.getHand(player.getUsername()).get(1).add(new Card(1, 10, 1));
        t.nextMove();
        t.stand();
        t.getResults(player.getUsername()).get(0).setPlayer(player);
        t.getResults(player.getUsername()).get(1).setPlayer(p2);
        t.stand();

        if(t.getDealer().getBlackjack()){ // Führt in zufälliger Reihenfolge aus: Stimmt, wenn es nach der Evaluierung ausgeführt wird.
            assertEquals(800, player.getWealth());
        }else if(t.getDealer().getScore() == 21){
            assertEquals(900, player.getWealth());
        }else{
            assertEquals(1000, player.getWealth());
        }
    }

    @Test
    public void testHit() throws DeckEmptyException, InvalidMoveException, InterruptedException {
        table.addPlayer(player);
        table.setStake(player, 100); // Spieler in activePlayers machen
        table.handCard(card, player);
        table.hit();
        assertEquals(2, table.getHand(player.getUsername()).get(0).size());
    }

    @Test
    public void testStand() throws DeckEmptyException, InvalidMoveException, InterruptedException {
        table.addPlayer(player);
        table.setStake(player, 100); // Spieler in activePlayers machen
        table.beginRound();
        table.stand();
        assertEquals(2, table.getHand(player.getUsername()).get(0).size());
    }

    @Test
    public void doubleDownInvalid() throws InvalidMoveException, DeckEmptyException {
        table.addPlayer(player);
        table.setStake(player, 100); // Spieler in activePlayers machen
        table.beginRound();
        table.handCard(card, player);
        assertFalse(table.isDdAllowed());
    }

    @Test
    public void testDoubleDown() throws DeckEmptyException, InvalidMoveException, InterruptedException {
        table.addPlayer(player);
        table.setStake(player, 100); // Spieler in activePlayers machen
        table.beginRound();
        int stake = table.getStake(player.getUsername()).get(0);
        table.doubleDown();
        assertEquals(stake * 2, table.getStake(player.getUsername()).get(0).intValue());
    }

    @Test
    public void testSplitInvalid() throws InvalidMoveException, DeckEmptyException {
        table.addPlayer(player);
        table.setStake(player, 100); // Spieler in activePlayers machen
        table.beginRound();
        table.handCard(card, player);
        assertFalse(table.isSplitAllowed());
    }

    @Test
    public void testSplit() throws DeckEmptyException, InvalidMoveException, InterruptedException {
        table.addPlayer(player);
        table.setStake(player, 100);
        table.beginRound();
        ArrayList<Card> playerCards = new ArrayList<>();
        playerCards.add(new Card(1, 1, 0)); // Ass Herz
        playerCards.add(new Card(1, 10, 0)); // 10 Herz
        table.getHand(player.getUsername()).set(0, playerCards);
        table.split();
        assertFalse(table.getHand(player.getUsername()).get(1).isEmpty());
    }

    @Test
    public void testSurrenderInvalid() throws InvalidMoveException, DeckEmptyException {
        table.addPlayer(player);
        table.setStake(player, 100); // Spieler in activePlayers machen
        table.beginRound();
        table.handCard(card, player);
        assertFalse(table.isSurrenderAllowed());
    }

    @Test
    public void testSurrender() throws DeckEmptyException, InvalidMoveException{
        table.addPlayer(player);
        table.setStake(player, 100); // Spieler in activePlayers machen
        table.beginRound();
        table.surrender();
        assertEquals(0, table.getResults(player.getUsername()).get(0).getScore());
    }

    @Test
    public void testGetCardScore() throws InvalidMoveException {
        table.addPlayer(player);
        table.setStake(player, 100); // Spieler in activePlayers machen
        ArrayList<Card> playerCards = new ArrayList<>();
        playerCards.add(new Card(1, 1,0));
        playerCards.add(new Card(1, 11,0)); // Blackjack
        table.getHand(player.getUsername()).set(0, playerCards);
        assertEquals(21, table.getCardScore());
    }

    @Test
    public void testNextPlayer() throws DeckEmptyException, InvalidMoveException, InterruptedException {
        table.addPlayer(player);
        table.setStake(player, 100); // Spieler in activePlayers machen
        Player p2 = new Player("player2", 1000);
        table.addPlayer(p2);
        table.setStake(p2, 100); // Spieler in activePlayers machen
        table.beginRound();
        table.nextPlayer();
        assertEquals("player2", table.getCurrentPlayer().getUsername());
    }

    @Test
    public void testEvaluateGame() throws InvalidMoveException, DeckEmptyException, InterruptedException {
        table.addPlayer(player);
        table.setStake(player, 100); // Spieler in activePlayers machen
        table.beginRound();
        ArrayList<Card> playerCards = new ArrayList<>();
        playerCards.add(new Card(1, 1,0));
        playerCards.add(new Card(1, 11,0)); // Blackjack
        table.getHand(player.getUsername()).set(0, playerCards);
        table.getResults(player.getUsername()).add(0, new WinningsEntry(100, 21, true));
        table.nextPlayer();
        if(table.getDealer().getBlackjack()){
            assertEquals(1000, player.getWealth()); // Spieler hat Gleichstand
        }else{
            assertTrue(player.getWealth() > 1000); // Spieler hat gewonnen
        }
        assertTrue(table.isGameFinished());
    }

    @Test
    public void testRestartGame() throws DeckEmptyException, InvalidMoveException, InterruptedException {
        table.addPlayer(player);
        table.setStake(player, 100);
        table.beginRound();
        table.stand();
        int groesse = table.getDeck().getSize();
        table.restartGame();
        assertNotEquals(groesse, table.getDeck().getSize());
    }
}

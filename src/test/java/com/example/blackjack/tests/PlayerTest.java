package com.example.blackjack.tests;
import com.example.blackjack.utils.game.exceptions.InvalidMoveException;
import com.example.blackjack.utils.game.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;


public class PlayerTest {
    @Test
    public void testPlayer1() throws InvalidMoveException {
        Player Player1 = new Player("string",20);
        Player1.init();
        Assertions.assertEquals("string", Player1.getUsername());
        Assertions.assertEquals(20, Player1.getWealth());
    }
    @Test
    public void testPlayer2(){
        Player Player2 = new Player("string",2);
        Assertions.assertEquals(2, Player2.getWealth());
        Player2.addWealth(5);
        Assertions.assertEquals(7, Player2.getWealth());
    }
    @Test
    public void testPlayer3(){
        Player Player3 = new Player("string",2);
        Assertions.assertEquals(2, Player3.getWealth());
        Player3.addWealth(5);
        Player3.setWealth(3);
        Assertions.assertEquals(3, Player3.getWealth());

    }
    @Test
    public void TestPlayer4(){
        Player Player4 = new Player("string",2);
        Assertions.assertEquals(2, Player4.getWealth());
        Player4.setWealth(5);
        Player4.addWealth(3);
        Assertions.assertEquals(8, Player4.getWealth());

    }
}


package com.example.blackjack.tests;
import com.example.blackjack.utils.game.Table;
import com.example.blackjack.utils.game.player.Player;
import com.example.blackjack.utils.game.player.bots.AdvancedBot;
import com.example.blackjack.utils.game.player.bots.SimpleBot;
import com.example.blackjack.utils.server.Database;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlayerBotTest {
    private Table table;
    private Player player;
    private SimpleBot Sbot;
    private AdvancedBot Abot;
    private Database database;


    @Before
    public void setUp() {
        database = new Database();
        table = new Table("test", database);
        player = new Player("testPlayer", 1000);
        Sbot = new SimpleBot("SimpleTest", 1000, database, table.getTableName());
        Abot = new AdvancedBot("AdvancedTest", 1000, database, table.getTableName());
        table.setCurPlayer(player);
    }

    @Test
    public void testGetName() {
        String Sname = Sbot.getUsername();
        String Aname = Abot.getUsername();
        assertEquals("SimpleTest", Sname);
        assertEquals("AdvancedTest", Aname);
    }

    @Test
    public void testGetWealth() {
        int Swealth = Sbot.getWealth();
        int Awealth = Abot.getWealth();
        assertEquals(1000, Swealth);
        assertEquals(1000, Awealth);
    }


}
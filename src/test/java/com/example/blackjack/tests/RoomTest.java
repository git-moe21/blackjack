package com.example.blackjack.tests;

import com.example.blackjack.utils.game.Room;
import com.example.blackjack.utils.game.player.Player;
import com.example.blackjack.utils.game.player.bots.AdvancedBot;
import com.example.blackjack.utils.game.player.bots.SimpleBot;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RoomTest {
    private Room room, roomBots;
    private Player p1, p2, p3;
    private AdvancedBot ab;
    private SimpleBot sb;

    @Before
    public void setUp() {
        room = new Room("testRoom", 0, 0, null);
        roomBots = new Room("testRoom", 2, 2, null);

        p1 = new Player("tester1", 1000);
        p2 = new Player("tester2", 1000);
        p3 = new Player("tester3", 1000);
        sb = new SimpleBot("simpleBot", 1000, null, "testRoom");
        ab = new AdvancedBot("advancedBot", 1000, null, "testRoom");
    }

    @Test
    public void tester1() {
        assertEquals("testRoom", room.getRoom_name());

        room.addPlayer(p1.getUsername(), p1.getWealth());
        room.addPlayer(p1.getUsername(), p1.getWealth());
        room.addPlayer(p2.getUsername(), p2.getWealth());
        room.addPlayer(p3.getUsername(), p3.getWealth());
        room.addPlayer(ab.getUsername(), ab.getWealth());
        room.addPlayer(sb.getUsername(), sb.getWealth());
        assertEquals(p1.getUsername(), room.getPlayers().get(0).getUsername());
        assertEquals(p2.getUsername(), room.getPlayers().get(1).getUsername());
        assertEquals(p3.getUsername(), room.getPlayers().get(2).getUsername());
        room.removePlayer(p1.getUsername());
        room.removePlayer("noUser");
        for (Player p : room.getPlayers()) {
            assertNotEquals(p.getUsername(), p1.getUsername());
        }
        assertEquals(4, room.getPlayers().size());
        assertEquals(4, room.getPlayerCount());

        assertEquals("testRoom:4:simpleBot:advancedBot:tester3:tester2", room.toString());
    }

    @Test
    public void tester2() {
        roomBots.addPlayer(p1.getUsername(), p1.getWealth());
        roomBots.addPlayer(p2.getUsername(), p2.getWealth());
        roomBots.addPlayer(p3.getUsername(), p3.getWealth());
        roomBots.addPlayer(ab.getUsername(), ab.getWealth());
        int roomSize = roomBots.getPlayerCount();
        roomBots.addPlayer(sb.getUsername(), sb.getWealth());
        assertEquals(roomSize, roomBots.getPlayerCount());
    }


}
package com.example.blackjack.tests;

import com.example.blackjack.utils.game.exceptions.InvalidMoveException;
import com.example.blackjack.utils.server.Database;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseTest {
    private Database database;

    @BeforeAll
    public void setUp() {
        this.database = new Database();
    }

    @AfterEach
    public void tearDown() {
        this.database =  new Database();
    }

    @Test
    public void testAddUser() {
        database.addUser("testUser", "testPass");

        assertTrue(database.duplicateUsername("testUser"));
        database.deleteUser("testUser");
    }

    @Test
    public void testDeleteUser() {
        database.addUser("testUser", "testPass");
        database.deleteUser("testUser");
        assertFalse(database.duplicateUsername("testUser"));
    }

    @Test
    public void testAuthenticate() {
        database.addUser("testUser", "testPass");

        assertEquals("testUser", database.authenticate("testUser", "testPass"));
        assertNull(database.authenticate("testUser", "wrongPass"));
        database.deleteUser("testUser");
    }

    @Test
    public void testGetActiveUsers() {
        database.addUser("testUser", "testPass");
        database.authenticate("testUser", "testPass");

        assertEquals("testUser:", database.getActiveUsers());
        database.deleteUser("testUser");
    }

    @Test
    public void testScoreboard() {
        database.addUser("testUser", "testPass");

        assertTrue(database.getScoreboard().contains("testUser:500@"));
        database.deleteUser("testUser");
    }

    @Test
    public void testSaveChatMessage() {
        database.saveChatMessage("testUser", "Hello, world!");

        List<String> messages = database.getChatMessages();
        assertEquals(1, messages.size());
        assertEquals("testUser: Hello, world!", messages.get(0));
    }

    @Test
    public void testRoomManagement() throws InvalidMoveException {
        database.addRoom("testRoom", 0, 0);
        database.joinRoom("testRoom", "testUser");

        assertTrue(database.getActiveRooms().contains("testRoom"));
    }

    @Test
    public void testStartGame() throws InvalidMoveException {
        database.addRoom("testRoom", 0, 0);
        database.joinRoom("testRoom", "testUser");
        database.startGame("testRoom");

        assertTrue(database.hasGameStarted("testRoom"));
    }

    @Test
    public void testChatMessages() {
        database.saveChatMessage("user1", "Hello!");
        database.saveChatMessage("user2", "Hi!");

        List<String> messages = database.getChatMessages();
        assertEquals(2, messages.size());
        assertEquals("user1: Hello!", messages.get(0));
        assertEquals("user2: Hi!", messages.get(1));
    }

    @Test
    public void testAuthenticateFailure() {
        assertNull(database.authenticate("nonExistentUser", "somePass"));
    }

    @Test
    public void testDuplicateUsername() {
        database.addUser("testUser", "testPass");
        assertTrue(database.duplicateUsername("testUser"));
        assertFalse(database.duplicateUsername("newUser"));
        database.deleteUser("testUser");
    }

    @Test
    public void testAddRoomWithoutBots() throws InvalidMoveException {
        database.addRoom("testRoomNoBots", 0, 0);
        String roomInfo = database.reloadRoomInfo("testRoomNoBots");

        assertTrue(roomInfo.contains("testRoomNoBots"));
        assertFalse(roomInfo.contains("#BotSimple"));
        assertFalse(roomInfo.contains("*BotAdvanced"));
    }

    @Test
    public void testAuthenticateInvalidUser() {
        assertNull(database.authenticate("nonExistentUser", "anyPass"));
    }

    @Test
    public void testAddMultipleRooms() throws InvalidMoveException {
        database.addRoom("room1", 1, 1);
        database.addRoom("room2", 2, 2);

        assertTrue(database.getActiveRooms().contains("room1"));
        assertTrue(database.getActiveRooms().contains("room2"));
    }

    @Test
    public void testChatMessageOrder() {
        database.saveChatMessage("user1", "First message");
        database.saveChatMessage("user2", "Second message");

        List<String> messages = database.getChatMessages();
        assertEquals("user1: First message", messages.get(0));
        assertEquals("user2: Second message", messages.get(1));
    }

    @Test
    public void testRoomWithNoBots() throws InvalidMoveException {
        database.addRoom("roomNoBots", 0, 0);
        String roomInfo = database.reloadRoomInfo("roomNoBots");

        assertTrue(roomInfo.contains("roomNoBots"));
        assertFalse(roomInfo.contains("#BotSimple"));
        assertFalse(roomInfo.contains("*BotAdvanced"));
    }

    @Test
    public void testBotOperations() throws InvalidMoveException {
        database.addRoom("roomWithBots", 1, 1);
        database.removeSimpleBot("roomWithBots");
        database.removeHardBot("roomWithBots");

        String roomInfo = database.reloadRoomInfo("roomWithBots");
        assertFalse(roomInfo.contains("#BotSimple"));
        assertFalse(roomInfo.contains("*BotAdvanced"));
    }
}

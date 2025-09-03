package com.example.blackjack.tests;

import com.example.blackjack.utils.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ServerTest {

    private Server server;
    private ServerSocket serverSocket;

    @BeforeEach
    void setUp() {
        try {
            serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();
            server = new Server(port, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testServerStart() {
        assertDoesNotThrow(() -> {
            // Run the server in a separate thread
            Thread serverThread = new Thread(server);
            serverThread.start();
            serverThread.join(1000);
        });
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            try {
                serverSocket.close();
                server = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
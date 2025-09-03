package com.example.blackjack.utils.server;

import java.io.*;
import java.net.*;

/**
 * Diese Klasse stellt einen Server dar, der auf Verbindungen von Clients wartet und diese
 * verarbeitet. Der Server nutzt eine Datenbank und akzeptiert eine bestimmte Anzahl von Clients.
 */
public class Server implements Runnable {
    private Database database = new Database();
    private ServerSocket serverSocket;
    private int anz_clients;

    /**
     * Konstruktor für die Server-Klasse. Initialisiert den Server-Socket auf dem angegebenen Port
     * und setzt die maximale Anzahl von Clients.
     *
     * @param port der Port, auf dem der Server lauscht.
     * @param anz_clients die maximale Anzahl von Clients, die der Server akzeptieren soll.
     * @throws IOException wenn ein I/O-Fehler beim Öffnen des Sockets auftritt.
     */
    public Server(int port, int anz_clients) throws IOException {
        serverSocket = new ServerSocket(port);
        this.anz_clients = anz_clients;
    }

    /**
     * Startet den Server und wartet auf Verbindungen von Clients. Sobald eine Verbindung
     * akzeptiert wird, wird ein neuer ClientHandler-Thread gestartet, um die Kommunikation
     * mit dem Client zu übernehmen.
     *
     * Diese Methode läuft solange, bis die maximale Anzahl von Clients erreicht ist.
     *
     * Ausnahmen:
     * @throws IOException wenn ein I/O-Fehler beim Akzeptieren einer Client-Verbindung auftritt.
     */
    @Override
    public void run() {
        System.out.println("[Server] Server Start");
        for (int i = 0; i <= anz_clients; i++) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("[Server] Verbunden mit " + socket.getRemoteSocketAddress());
                new ClientHandler(socket, database).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

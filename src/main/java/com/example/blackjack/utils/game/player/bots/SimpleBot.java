package com.example.blackjack.utils.game.player.bots;

import com.example.blackjack.utils.game.Table;
import com.example.blackjack.utils.game.exceptions.DeckEmptyException;
import com.example.blackjack.utils.game.exceptions.InvalidMoveException;
import com.example.blackjack.utils.game.player.Player;
import com.example.blackjack.utils.server.Database;

import java.util.*;

/**
 * Ein einfacher Bot für das Blackjack-Spiel, der grundlegende Entscheidungen trifft.
 * Der Bot führt Aktionen wie Hit oder Stand basierend auf der Punktzahl der Karten aus.
 */
public class SimpleBot extends Player {
    private String table_name;
    private Database database;
    private Timer timer;
    private Table table;

    /**
     * Konstruktor für die Klasse SimpleBot.
     *
     * @param name        Der Name des Bots.
     * @param wealth      Das Vermögen des Bots.
     * @param database    Die Datenbankverbindung zur Verwaltung der Spielaktionen.
     * @param table_name  Der Name des Tisches, an dem der Bot spielt.
     */
    public SimpleBot(String name, int wealth, Database database, String table_name){
        super(name, wealth);
        this.database = database;
        this.table_name= table_name;
    }

    /**
     * Initialisiert den Bot, indem der entsprechende Tisch aus der Datenbank geladen wird.
     * Startet einen Timer, der regelmäßig Entscheidungen des Bots trifft.
     * Der Timer wird beendet, wenn das Spiel des Bots beendet ist.
     */
    @Override
    public void init() {
        //System.out.println("[Server] Initialisiere Bot " + getUsername() + " für Tisch " + table_name);

        for (Table t : database.getActive_tables()) {
            if (t.getTableName().equals(table_name)) {
                this.table = t;
            }
        }

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (table.isBotGame()) {
                        timer.cancel();
                        timer.purge();
                    } else {
                        Gamemove();
                    }
                } catch (Exception | DeckEmptyException e) {
                    e.printStackTrace();
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 0, 1000);
    }

    /**
     * Führt die nächste Aktion des Bots im Spiel aus.
     * Der Bot entscheidet basierend auf der Punktzahl der Karten:
     * - **Hit:** Wenn die Kartenpunktzahl unter 17 liegt.
     * - **Stand:** Wenn die Kartenpunktzahl 17 oder höher liegt.
     *
     * Der Bot setzt auch einen Einsatz, wenn der Countdown 20 beträgt.
     *
     * @throws InvalidMoveException Falls ein ungültiger Zug auftritt.
     * @throws DeckEmptyException   Falls der Deck leer ist.
     * @throws InterruptedException Falls der Thread unterbrochen wird.
     */
    public void Gamemove() throws InvalidMoveException, DeckEmptyException, InterruptedException {
        if (!table.isGameFinished()) {
            if (table.getCountdown() == 20) {
                database.setStake(table_name, getUsername(),100);

            } else if (!(table.getCurrentPlayer() == null)  && table.getCurrentPlayer().equals(this)) {
                Thread.sleep(5000); // Bot lässt sich etwas Zeit mit dem Spielzug...
                if (!(table.getCurrentPlayer() == null)) { // Es kann sein, dass während des Schlafens das Spiel beendet wurde, somit ist getCurrenPlayer wieder null
                    if (table.getCardScore() < 17){
                        database.hit(table_name);
                    } else {
                        database.stand(table_name);
                    }
                }
            }
        }
    }
}
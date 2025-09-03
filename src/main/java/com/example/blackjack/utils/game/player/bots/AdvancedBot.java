package com.example.blackjack.utils.game.player.bots;

import com.example.blackjack.utils.game.Table;
import com.example.blackjack.utils.game.exceptions.DeckEmptyException;
import com.example.blackjack.utils.game.exceptions.InvalidMoveException;
import com.example.blackjack.utils.game.player.Player;
import com.example.blackjack.utils.server.Database;

import java.util.*;

/**
 * Ein fortgeschrittener Bot für das Blackjack-Spiel, der automatisch Entscheidungen trifft.
 * Dieser Bot führt Aktionen wie Hit, Stand, Double Down und Surrender basierend auf dem aktuellen Spielzustand aus.
 */
public class AdvancedBot extends Player {
    private String table_name;
    private Database database;
    private Timer timer;
    private Table table;

    /**
     * Konstruktor für die Klasse AdvancedBot.
     *
     * @param name        Der Name des Bots.
     * @param wealth      Das Vermögen des Bots.
     * @param database    Die Datenbankverbindung zur Verwaltung der Spielaktionen.
     * @param table_name  Der Name des Tisches, an dem der Bot spielt.
     */
    public AdvancedBot(String name, int wealth, Database database, String table_name){
        super(name, wealth);;
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
     * Der Bot entscheidet basierend auf dem aktuellen Punktestand der Karten und den erlaubten Spielzügen.
     * Folgende Aktionen werden durchgeführt:
     * - **Hit:** Wenn die Kartenpunktzahl unter 17 liegt.
     * - **Surrender:** Wenn die Kartenpunktzahl unter 6 liegt und Surrender erlaubt ist.
     * - **Double Down:** Wenn die Kartenpunktzahl zwischen 11 und 16 liegt und Double Down erlaubt ist.
     * - **Stand:** Wenn die Kartenpunktzahl über 17 liegt.
     *
     * @throws InvalidMoveException Falls ein ungültiger Zug auftritt.
     * @throws DeckEmptyException   Falls der Deck leer ist.
     * @throws InterruptedException Falls der Thread unterbrochen wird.
     */
    public void Gamemove() throws InvalidMoveException, DeckEmptyException, InterruptedException {
        if (table.getCountdown() == 20) {
            database.setStake(table_name,getUsername(),100);

        } else if (!(table.getCurrentPlayer() == null)  && table.getCurrentPlayer().equals(this)) {
            Thread.sleep(5000); // Bot lässt sich etwas Zeit mit dem Spielzug...

            if (!(table.getCurrentPlayer() == null)) { // Es kann sein, dass während des Schlafens das Spiel beendet wurde, somit ist getCurrenPlayer wieder null
                int cardscore = table.getCardScore();

                if (cardscore < 17){
                    database.hit(table_name);
                }

                if (cardscore < 6){
                    if (database.isSurrenderAllowedBot(table_name)) {
                        database.surrender(table_name);
                    }
                }

                if (cardscore > 11 && cardscore < 16){
                    if (database.isDdAllowedBot(table_name)){
                        database.doubledown(table_name);
                    }
                }

                if (cardscore >= 17){
                    database.stand(table_name);
                }
            }
        }
    }
}
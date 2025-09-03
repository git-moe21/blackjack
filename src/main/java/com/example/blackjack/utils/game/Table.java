package com.example.blackjack.utils.game;

import com.example.blackjack.utils.game.exceptions.DeckEmptyException;
import com.example.blackjack.utils.game.exceptions.InvalidMoveException;
import com.example.blackjack.utils.game.player.Player;
import com.example.blackjack.utils.game.player.bots.AdvancedBot;
import com.example.blackjack.utils.game.player.bots.SimpleBot;
import com.example.blackjack.utils.server.Database;
import javafx.application.Platform;
import javafx.scene.chart.PieChart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Die Klasse verwaltet den Zustand eines Blackjack-Spiels.
 * Sie kümmert sich um das Verteilen der Karten, das Setzen der Einsätze, die Verwaltung der Spieler und das Überprüfen der Spielregeln.
 */
public class Table {
    private String table_name;
    private Deck deck = new Deck();
    private Dealer dealer = new Dealer(deck, this);
    private ArrayList<Player> players;
    private ArrayList<Player> activePlayers;
    private ArrayList<String> playersWhoAlreadyGotResult;
    private Player currentPlayer;
    private int playerPos = 0;
    int currentPlayerCardPointer = 0; // 0 oder 1
    boolean split = false;
    boolean doubleDown = false;
    int countdown;
    Timer timer;
    boolean gameFinished = false;
    private Database database;
    private HashMap<String, ArrayList<Integer>> stakes = new HashMap<>();
    private HashMap<String, ArrayList<ArrayList<Card>>> gameCards = new HashMap<>();
    private HashMap<String, ArrayList<WinningsEntry>> winnings = new HashMap<>();

    /**
     * Konstruktor für die Klasse.
     * Initialisiert den Tisch mit einem Namen und startet den Countdown für den automatischen Spielbeginn.
     *
     * @param table_name der Name des Tisches
     */
    public Table(String table_name, Database database) {
        this.database = database;
        this.table_name = table_name;
        this.players = new ArrayList<>();
        this.activePlayers = new ArrayList<>();
        this.playersWhoAlreadyGotResult = new ArrayList<>();
        this.countdown = 30;

        timer = new Timer(true); // Countdown zum automatischen Spielstart
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Platform.runLater(() -> {
                        //System.out.println("Tisch " + table_name + " läuft");
                        if (countdown <= 5) {
                            try {
                                if (isBotGame() || players.isEmpty() || countdown == 0) { // Keine Spieler oder nur Bots im Spiel, Spiel beenden
                                    timer.cancel();
                                    timer.purge();

                                } else if (activePlayers.isEmpty()) { // Spieler in Raum, Niemand hat Einsatz abgegeben
                                    System.out.println("[Server] Kein Spieler an Tisch " + table_name + " hat einen Einsatz abgegeben, starte Countdown erneut");
                                    countdown = 30;

                                } else if (countdown == 3){
                                    beginRound();
                                    countdown-=1;

                                } else {
                                    countdown-=1;
                                }

                            } catch (DeckEmptyException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            countdown-=1;
                        }
                    });
                } catch (Exception e) {
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 0, 1000);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<Player> getActivePlayers() {
        return activePlayers;
    }

    public int getCountdown() {
        return countdown;
    }

    /**
     * Initialisiert alle Bots am Tisch.
     *
     * @throws InvalidMoveException wenn ein Bot einen ungültigen Zug ausführt
     */
    public void initializeBots() throws InvalidMoveException {
        for (Player p : players) {
            if (p instanceof SimpleBot || p instanceof AdvancedBot) {
                p.init();
            }
        }
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public ArrayList<WinningsEntry> getResults(String user_name) {
        return (winnings.get(user_name));
    }

    public ArrayList<Integer> getStake(String user_name) {
        return stakes.get(user_name);
    }

    /**
     * Fügt den Spieler zur Liste der Spieler hinzu, die bereits ein Ergebnis erhalten haben.
     *
     * @param user_name der Name des Spielers
     */
    public void addResultPlayer(String user_name) {
        if (!playersWhoAlreadyGotResult.contains(user_name)) {
            playersWhoAlreadyGotResult.add(user_name);
        }
    }

    /**
     * Überprüft, ob der Spieler bereits ein Ergebnis erhalten hat.
     *
     * @param user_name der Name des Spielers
     * @return {@code true} wenn der Spieler bereits ein Ergebnis erhalten hat, andernfalls {@code false}
     */
    public boolean alreadyGotResult(String user_name) {
        return (playersWhoAlreadyGotResult.contains(user_name));
    }

    public String getTableName() {
        return table_name;
    }

    /**
     * Überprüft, ob es sich bei dem aktuellen Spiel um ein Bot-Spiel handelt.
     *
     * @return true wenn alle Spieler Bots sind, andernfalls false
     */
    public boolean isBotGame() {
        boolean tmp = false;
        for (Player p : players) {
            if (p.getClass().equals(SimpleBot.class) || p.getClass().equals(AdvancedBot.class)) {
                tmp = true;
            } else {
                return false;
            }
        }
        return tmp;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public ArrayList<ArrayList<Card>> getHand(String user_name) {
        return gameCards.get(user_name);
    }

    public void setCurPlayer(Player p){
        currentPlayer = p;
    }

    public Deck getDeck() {
        return deck;
    }

    /**
     * Entfernt einen Spieler vom Tisch.
     *
     * @param user_name der Name des Spielers, der entfernt werden soll
     * @throws DeckEmptyException wenn das Deck leer ist
     */
    public void removePlayer(String user_name) throws DeckEmptyException {
        for (Player p : players) {
            if (p.getUsername().equals(user_name)) {
                players.remove(p);
                break;
            }
        }

        for (Player p : activePlayers) {
            if (p.getUsername().equals(user_name)) {
                if (!(currentPlayer == null) && currentPlayer.equals(p)) {
                    nextPlayer();
                    playerPos--;
                }
                activePlayers.remove(p);
                break;
            }
        }
    }

    /**
     * Setzt das Spiel zurück und startet es neu.
     */
    public void restartGame() {
        // Reset Arrays vom vorherigen Spiel
        //System.out.println("[Server] Starte Spiel an Tisch " + table_name + " neu");

        this.activePlayers = new ArrayList<>();
        this.gameCards = new HashMap<>();
        this.winnings = new HashMap<>();
        this.stakes = new HashMap<>();
        this.playersWhoAlreadyGotResult = new ArrayList<>();

        this.playerPos = 0;
        this.countdown = 30;
        this.doubleDown = false;
        this.split = false;
        this.gameFinished = false;
        this.currentPlayerCardPointer = 0;
        this.currentPlayer = null;

        this.deck = new Deck();
        this.dealer = new Dealer(deck, this);

        timer = new Timer(true); // Countdown zum automatischen Spielstart
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Platform.runLater(() -> {
                        //System.out.println("Tisch " + table_name + " läuft");
                        if (countdown <= 5) {
                            try {
                                if (isBotGame() || players.isEmpty() || countdown == 0) { // Keine Spieler oder nur Bots im Spiel, Spiel beenden
                                    timer.cancel();
                                    timer.purge();

                                } else if (activePlayers.isEmpty()) { // Spieler in Raum, Niemand hat Einsatz abgegeben
                                    System.out.println("[Server] Kein Spieler an Tisch " + table_name + " hat einen Einsatz abgegeben, starte Countdown erneut");
                                    countdown = 30;

                                } else if (countdown == 3){
                                    beginRound();
                                    countdown-=1;

                                } else {
                                    countdown-=1;
                                }

                            } catch (DeckEmptyException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            countdown-=1;
                        }
                    });
                } catch (Exception e) {
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 0, 1000);
    }

    /**
     * weißt einem übergebenen Spieler die übergebene Karte zu
     *
     * @param card die Karte die der Spieler erhält.
     * @param player der Spieler der die Karte erhält.
     */
    public void handCard(Card card, Player player) {
        gameCards.get(player.getUsername()).get(0).add(card);
    }

    /**
     * Fügt einen Spieler dem Tisch hinzu.
     *
     * @param p der zu hinzufügende Player
     */
    public void addPlayer(Player p){
        this.players.add(p);
    }

    /**
     * Beginnt die Runde: Es wird jedem Spieler und dem Dealer eine Karte offen zugeteilt.
     * Danach erhält jeder Spieler noch eine weitere offene Karte und der Dealer eine verdeckte.
     *
     * @throws DeckEmptyException falls das Spieldeck leer sein sollte.
     */
    public void beginRound() throws DeckEmptyException { // ruft beginRound in Dealer auf
        System.out.println("[Server] Starte Spiel an Tisch " + table_name);

        dealer.beginRound(activePlayers);

        currentPlayer = activePlayers.get(0);
    }

    /**
     * setzt den Einsatz eines Spielers.
     *
     * @param player Ist der Spieler, um dessen Einsatz es geht
     * @param stake Ist der Einsatz der gesetzt werden soll
     * @throws InvalidMoveException Wenn der Spieler nicht genug Geld hat um stake zu setzen
     */
    public void setStake(Player player, int stake) throws InvalidMoveException {
        if (player.getWealth() < stake){
            throw new InvalidMoveException("Spieler hat nicht genug Geld, den Einsatz zu setzen!"); // Doppelte Überprüfung, wird auch vor Absenden auf Client gecheckt
        } else {
            if (!activePlayers.contains(player)) {
                activePlayers.add(player);
                ArrayList<Integer> s = new ArrayList<>();
                s.add(stake);
                stakes.put(player.getUsername(), s);
                player.setWealth(player.getWealth() - stake);

                // Init Hände
                ArrayList<Card> temp_hand_1= new ArrayList<>();
                ArrayList<Card> temp_hand_2= new ArrayList<>();
                ArrayList<ArrayList<Card>> temp = new ArrayList<>();
                temp.add(temp_hand_1);
                temp.add(temp_hand_2);

                gameCards.put(player.getUsername(), temp);

                ArrayList<WinningsEntry> winn_info = new ArrayList<>();
                winnings.put(player.getUsername(), winn_info);
            }
        }
    }

    /**
     * prüft, ob der aktuelle Spieler ein Blackjack hat
     *
     * @return Wahrheitswert, ob der Spieler ein Blackjack hat.
     */
    public boolean checkBlackjack(){
        ArrayList<Card> pointer = gameCards.get(currentPlayer.getUsername()).get(currentPlayerCardPointer);
        int card1 = pointer.get(0).getRank();
        int card2 = pointer.get(1).getRank();
        return (card1 == 1 && card2 >= 10) | (card2 == 1 && card1 >= 10); // true wenn Blackjack
    }

    /**
     * wird nach jedem Move eines Spielers aufgerufen und entscheidet, ob der Spieler erneut entscheiden darf, oder ob ein anderer Spieler dran ist, da er sich bereits überboten hat.
     *
     * @throws DeckEmptyException wenn der Dealer anschließend ein leeres Spieldeck haben sollte
     */
    public void nextMove() throws DeckEmptyException {
        //System.out.println("[Server] Nächster Move für Tisch " + table_name + " wird berechnet");
        ArrayList<Card> pointer = gameCards.get(currentPlayer.getUsername()).get(0);
        if(split){
            if(checkBlackjack()){
                split = false;
                winnings.get(currentPlayer.getUsername()).add(new WinningsEntry(stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer), getCardScore(), false));
                currentPlayerCardPointer++;
                return; // same Player
            }else{
                if(getCardScore() > 21){
                    winnings.get(currentPlayer.getUsername()).add(new WinningsEntry(stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer), getCardScore(), false));
                    split = false;
                    currentPlayerCardPointer++; // same Player, other hand
                }else if(doubleDown){
                    split = false;
                    doubleDown = false;
                    winnings.get(currentPlayer.getUsername()).add(new WinningsEntry(stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer), getCardScore(), false));
                    currentPlayerCardPointer++; // same Player, other hand
                }else if(pointer.get(0).getRank() == 1){
                    winnings.get(currentPlayer.getUsername()).add(new WinningsEntry(stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer), getCardScore(), false));
                    ArrayList<Card> pointer1 = gameCards.get(currentPlayer.getUsername()).get(1);
                    int rank = pointer1.get(1).getRank();
                    if(rank >= 10){
                        winnings.get(currentPlayer.getUsername()).add(new WinningsEntry(stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer), 21, true));
                    }else if(rank == 1){
                        winnings.get(currentPlayer.getUsername()).add(new WinningsEntry(stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer), 12, false));
                    }else{
                        winnings.get(currentPlayer.getUsername()).add(new WinningsEntry(stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer), rank + 11, false));
                    }
                    nextPlayer();
                }else{
                    return; //same Player
                }
            }
        }else{
            if(currentPlayerCardPointer == 1 && checkBlackjack()){
                winnings.get(currentPlayer.getUsername()).add( new WinningsEntry(stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer), 21, false));
                nextPlayer();
            }else if(checkBlackjack()){
                winnings.get(currentPlayer.getUsername()).add( new WinningsEntry(stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer), 21, true));
                nextPlayer();
            }else{
                if(getCardScore() > 21){
                    winnings.get(currentPlayer.getUsername()).add( new WinningsEntry(stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer), getCardScore(), false));
                    nextPlayer();
                }else if(doubleDown){
                    winnings.get(currentPlayer.getUsername()).add( new WinningsEntry(stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer), getCardScore(), false));
                    nextPlayer();
                }else{
                    return; //same Player
                }
            }
        }
    }

    /**
     * Lässt den Spieler eine neue Karte ziehen
     */
    public void hit() throws DeckEmptyException {
        gameCards.get(currentPlayer.getUsername()).get(currentPlayerCardPointer).add(deck.dealCard());
        nextMove();
    }

    /**
     * beenden eines Zuges. Wenn die Hand noch nicht überboten ist,
     * beendet stand den Zug und fährt entweder mit dem nächsten Spieler
     * oder der nächsten Hand des gleichen Spielers fort
     *
     * @throws DeckEmptyException wenn der Dealer anschließend ein leeres Spieldeck haben sollte
     */
    public void stand() throws DeckEmptyException {
        winnings.get(currentPlayer.getUsername()).add(currentPlayerCardPointer, new WinningsEntry(stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer), getCardScore(), false));
        nextPlayer();
    }

    /**
     * Spieler kann nach den ersten beiden Karten seinen Einsatz verdoppeln. Anschließend erhält er genau eine Karte.
     *
     * @throws DeckEmptyException wenn das Spieldeck leer sein sollte.
     */
    public void doubleDown() throws DeckEmptyException {
        doubleDown = true;
        gameCards.get(currentPlayer.getUsername()).get(currentPlayerCardPointer).add(deck.dealCard());
        currentPlayer.addWealth(-stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer));
        stakes.get(currentPlayer.getUsername()).add(currentPlayerCardPointer, stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer) * 2);

        nextMove();
    }

    /**
     * Prüft, ob man aktuell einen DD ausführen darf
     *
     * @return true, falls DD erlaubt ist
     */
    public boolean isDdAllowed(){
        if(hasThirdCard()){
            return false; // Spieler hat bereits eine dritte Karte erhalten (DD)!
        }
        return currentPlayer.getWealth() >= stakes.get(currentPlayer.getUsername()).get(0); //Spieler hat nicht genug Geld, um DD durchzuführen
    }

    /**
     * prüft, ob der Spieler bereits eine dritte Karte erhalten hat (Hilfsfunktion für doubleDown und surrender)
     *
     * @return Wahrheitswert, ob eine dritte Karte erhalten wurde.
     */
    private boolean hasThirdCard(){
        int numberCards = 0;

        for (int i = 0; i < gameCards.get(currentPlayer.getUsername()).size(); i++){
            numberCards += gameCards.get(currentPlayer.getUsername()).get(i).size();
        }
        return numberCards > 2;
    }

    /**
     * Spieler kann seine Hand teilen, wenn die ersten beiden Karten gleichwertig sind. Mehrfaches Teilen ist nicht möglich
     *
     * @throws DeckEmptyException, falls das Deck leer sein sollte.
     */
    public void split() throws DeckEmptyException {
        ArrayList<Card> pointer = gameCards.get(currentPlayer.getUsername()).get(0);
        ArrayList<Card> pointer1 = gameCards.get(currentPlayer.getUsername()).get(1);

        split = true;
        Card c1 = deck.dealCard();
        Card c2 = deck.dealCard();
        pointer1.add(pointer.get(1));
        pointer.remove(1);
        pointer.add(c1);
        pointer1.add(c2);
        stakes.get(currentPlayer.getUsername()).add(currentPlayerCardPointer + 1, stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer));
        currentPlayer.addWealth(-stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer));
        nextMove();

    }

    /**
     * Prüft, ob man aktuell einen Split ausführen darf
     *
     * @return true, falls splitten erlaubt ist
     */
    public boolean isSplitAllowed(){
        ArrayList<Card> pointer = gameCards.get(currentPlayer.getUsername()).get(0);
        if(pointer.size() != 2){
            return false; // Es kann zum aktuellen Zeitpunkt nicht gesplittet werden!
        }else{
            if((pointer.get(0).getRank() != pointer.get(1).getRank()) || (pointer.get(0).getRank() >= 10 && pointer.get(1).getRank() >= 10)){
                return false; // Es darf bei verschiedenen Karten nicht gesplittet werden!
            }
        }
        if(currentPlayer.getWealth() < stakes.get(currentPlayer.getUsername()).get(0)){
            return false; // Spieler hat nicht genug Geld, um einen Split durchzuführen
        }
        return gameCards.get(currentPlayer.getUsername()).get(1).isEmpty(); // Mehrmaliges Teilen nicht möglich
    }

    /**
     * Lässt den Spieler nach den ersten beiden Karten aufgeben und er erhält die Hälfte seines Einsatzes zurück.
     *
     * @throws DeckEmptyException wenn der Dealer anschließend ein leeres Spieldeck haben sollte
     */
    public void surrender() throws DeckEmptyException {
        winnings.get(currentPlayer.getUsername()).add(currentPlayerCardPointer, new WinningsEntry(stakes.get(currentPlayer.getUsername()).get(currentPlayerCardPointer), 0, false));
        nextPlayer();

    }

    /**
     * Prüft, ob man aufgeben darf, also noch keine dritte Karte erhalten wurde
     *
     * @return true, falls aufgegeben werden darf
     */
    public boolean isSurrenderAllowed(){
        return !hasThirdCard(); // Spieler hat bereits eine dritte Karte erhalten (SUR)!
    }

    /**
     * Gibt den Score der Hand des Spielers zurück
     * @param user_name der Name des Spieler der seine Hand wissen will
     * @param hand welche Hand ausgegeben werden soll
     * @return Score der Hand
     */
    public int getCardScoreOfPlayer(String user_name, int hand) {
        int score = 0;
        int anzAss = 0;
        try {
            ArrayList<Card> pointer = gameCards.get(user_name).get(hand);
            for(int i = 0; i <= pointer.size() - 1; i++){
                int rank = pointer.get(i).getRank();
                if(rank == 1){
                    score += 11;
                    anzAss++;
                }else if(rank >= 2 && rank <= 9){
                    score += rank;
                }else if(rank >= 10){
                    score += 10;
                }
            }
            while(score > 21 && anzAss > 0){
                score -= 10;
                anzAss--;
            }
            return score;

        } catch (NullPointerException e) {
            return 0;
        }

    }

    /**
     * Gibt den Karten-Score des aktuellen Spielers zurück
     *
     * @return Score der aktuellen Hand
     */
    public int getCardScore(){
        int score = 0;
        int anzAss = 0;
        ArrayList<Card> pointer = gameCards.get(currentPlayer.getUsername()).get(currentPlayerCardPointer);
        for(int i = 0; i <= pointer.size() - 1; i++){
            int rank = pointer.get(i).getRank();
            if(rank == 1){
                score += 11;
                anzAss++;
            }else if(rank >= 2 && rank <= 9){
                score += rank;
            }else if(rank >= 10){
                score += 10;
            }
        }
        while(score > 21 && anzAss > 0){
                score -= 10;
                anzAss--;
        }
        return score;
    }

    /**
     * ruft den nächsten Spieler auf, um mit seinem Zug weiterzumachen.
     *
     * @throws DeckEmptyException falls das Spieldeck leer sein sollte, wenn der Dealer seine Karten zieht
     */
    public void nextPlayer() throws DeckEmptyException {
        if(playerPos == activePlayers.size() - 1){
            dealer.makeMove();
            evaluateGame();
        }else{
            playerPos++;
            currentPlayer = activePlayers.get(playerPos);
            currentPlayerCardPointer = 0;
            split = false;
            doubleDown = false;
            nextMove();
        }
    }

    /**
     * warte 400ms damit die Clients genug Zeit haben sich die Results zu haben
     */
    public void waitTimeForClientsToReceiveResults() {
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                restartGame();
            }
        };

        timer.schedule(task, 400);
    }

    /**
     * evaluiert das Spiel. Zahlt jedem Spieler seinen Gewinn aus und zieht die Gewinne der Bank ein.
     * Gibt die Karten zurück ins Deck.
     * Ändert bei den Spielern den Score ab, bei Gewinn.
     */
    public void evaluateGame() {
        System.out.println("[Server] Das Spiel an Tisch " + table_name + " ist vorbei. -> Auswertung");
        int dealerScore = dealer.getScore();
        if (dealer.getBlackjack()) { // dealer blackjack
            System.out.println("[Server] An Tisch " + table_name + " hat Dealer Blackjack");
            for (Player player : activePlayers) {
                for(int i = 0; i <= winnings.get(player.getUsername()).size() - 1; i++) {
                    if (winnings.get(player.getUsername()).get(i).isBlackjack()){
                        if (player instanceof SimpleBot || player instanceof AdvancedBot) {
                            database.saveChatMessage("[Server]", "Bot " + player.getUsername() + " hat an Tisch " + table_name + " mit dem Dealer zusammen Blackjack ("+ player.getWealth() + ")");
                        }
                        player.addWealth(winnings.get(player.getUsername()).get(i).getStake()); // blackjack
                    } else if (winnings.get(player.getUsername()).get(i).getScore() == 0) {
                        if (player instanceof SimpleBot || player instanceof AdvancedBot) {
                            database.saveChatMessage("[Server]", "Bot " + player.getUsername() + " hat an Tisch " + table_name + " aufgegeben ("+ player.getWealth() + ")");
                        }
                        player.addWealth(winnings.get(player.getUsername()).get(i).getStake() / 2); // surrender
                    }
                }
            }
        } else {
            for (Player player : activePlayers) {
                ArrayList<WinningsEntry> pointer = winnings.get(player.getUsername());
                for (int i = 0; i < pointer.size(); i++) {
                    if (pointer.get(i).isBlackjack()) {
                        System.out.println("[Server] An Tisch " + table_name + " hat " + player.getUsername() + " mit Blackjack gewonnen");
                        player.addWealth(pointer.get(i).getStake() * 5 / 2); // Blackjack

                        if (player instanceof SimpleBot || player instanceof AdvancedBot) {
                            database.saveChatMessage("[Server]", "Bot " + player.getUsername() + " hat an Tisch " + table_name + " mit Blackjack gewonnen ("+ player.getWealth() + ")");
                        }

                    } else if(pointer.get(i).getScore() == 0) {
                        player.addWealth(pointer.get(i).getStake() / 2); // surrender

                        if (player instanceof SimpleBot || player instanceof AdvancedBot) {
                            database.saveChatMessage("[Server]", "Bot " + player.getUsername() + " hat an Tisch " + table_name + " aufgegeben ("+ player.getWealth() + ")");
                        }

                    } else if ((pointer.get(i).getScore() > dealerScore && pointer.get(i).getScore() <= 21) || (dealerScore > 21 && pointer.get(i).getScore() <= 21)) {
                        System.out.println("[Server] An Tisch " + table_name + " hat " + player.getUsername() + " gewonnen");
                        player.addWealth(pointer.get(i).getStake() * 2); // Gewinn

                        if (player instanceof SimpleBot || player instanceof AdvancedBot) {
                            database.saveChatMessage("[Server]", "Bot " + player.getUsername() + " hat an Tisch " + table_name + " gewonnen ("+ player.getWealth() + ")");
                        }

                    } else if (pointer.get(i).getScore() == dealerScore && dealerScore <= 21) {
                        System.out.println("[Server] An Tisch " + table_name + " hat " + player.getUsername() + " einen Gleichstand mit dem Dealer");
                        player.addWealth(pointer.get(i).getStake()); // Gleichstand

                        if (player instanceof SimpleBot || player instanceof AdvancedBot) {
                            database.saveChatMessage("[Server]", "Bot " + player.getUsername() + " hat an Tisch " + table_name + " einen Gleichstand mit dem Dealer ("+ player.getWealth() + ")");
                        }
                    }

                    if (((pointer.get(i).getScore() > 21) || (pointer.get(i).getScore() < dealerScore && dealerScore <= 21)) && (player instanceof SimpleBot || player instanceof AdvancedBot)) {
                        database.saveChatMessage("[Server]", "Bot " + player.getUsername() + " hat an Tisch " + table_name + " seinen Einsatz verloren ("+ player.getWealth() + ")");
                    }
                    if (player.getWealth() == 0) {
                        database.saveChatMessage("[Server]", player.getUsername() + " ist an Tisch " + table_name + " Pleite gegangen");
                    }
                }

            }
        }

        // Speichere Winnings

        for (Player p : activePlayers) {
            ArrayList<WinningsEntry> winning = winnings.get(p.getUsername());

            for (WinningsEntry w : winning) {
                w.setPlayer(p);
            }
        }

        this.gameFinished = true;
        waitTimeForClientsToReceiveResults();
    }
}

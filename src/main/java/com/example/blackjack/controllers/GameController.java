package com.example.blackjack.controllers;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
/**
 * Der GameController verwaltet die Benutzeroberfläche für das Blackjack-Spiel.
 * Diese Klasse ist verantwortlich für die Interaktion zwischen der Benutzeroberfläche und den
 * Spiel- und Serverlogik.
 */
public class GameController {
    @FXML
    public ListView chatView;
    @FXML
    public TextField chatField;
    @FXML
    private Pane root;
    @FXML
    public TextField stakeField;
    @FXML
    public Button setStakeButton, SurrenderButton, StandButton, HitButton, DDButton, SplitButton;
    @FXML
    private VBox playerSplitVBox1, playerSplitVBox2;
    @FXML
    private ImageView dealerImageView1, dealerImageView2, playerImageView1, playerImageView2, playerSplitImageView1, playerSplitImageView2;
    @FXML
    private Circle avatar1, avatar2, avatar3, avatar4, avatar5, avatar6, avatar7, avatar8;
    @FXML
    private Label player1Label, player2Label, player3Label, player4Label, player5Label, player6Label, player7Label, player8Label, playerBalanceLabel, timerLabel, winningsLabel, resultLabel, player1BetLabel, player2BetLabel, player3BetLabel, player4BetLabel ,player5BetLabel, player6BetLabel, player7BetLabel, player8BetLabel,player1ScoreLabel, player2ScoreLabel, player3ScoreLabel,player4ScoreLabel, player5ScoreLabel, player6ScoreLabel, player7ScoreLabel, player8ScoreLabel;

    private final Stage stage; // Die aktuelle Stage
    private final DataOutputStream out; // OutputStream zum Senden von Daten an den Server
    private final DataInputStream in; // InputStream zum Empfangen von Daten vom Server
    private final String user; // Der Benutzername des aktuellen Benutzers
    private int balance;
    private Timer timer;
    private final String table_name;
    private ArrayList<String> chat_messages;
    private ArrayList<ImageView> imageViewArray = new ArrayList<>();
    private ArrayList<Label> nameLabelList = new ArrayList<>();
    private ArrayList<Circle> circleList = new ArrayList<>();
    private Map<Label, Label> playerToBetLabelMap;
    private Map<Label, Label> playerToScoreLabelMap;
    private int count = 30;

    /**
     * Initialisiert den GameController. Setzt die Game-Initialisierung und startet den Timer,
     * um den Spielzustand regelmäßig neu zu laden.
     *
     * @throws IOException Wenn ein I/O-Fehler auftritt.
     */
    public void initialize() throws IOException {
       initializeGame();

       timer = new Timer(true);
       timer.scheduleAtFixedRate(new TimerTask() {
           @Override
           public void run() {
               try {
                   Platform.runLater(() -> {
                       try {
                           reloadGameState();
                           reloadChatMessages();
                       } catch (IOException e) {
                           timer.cancel();
                           timer.purge();
                       }
                   });
               } catch (Exception e) {
                   timer.cancel();
                   timer.purge();
               }
           }
       }, 0, 400);
    }

    /**
     * Konstruktor für den GameController.
     *
     * @param stage Die aktuelle Stage.
     * @param out Der OutputStream zum Senden von Daten an den Server.
     * @param in Der InputStream zum Empfangen von Daten vom Server.
     * @param user Der Benutzername des aktuellen Benutzers.
     * @param table_name Der Name des Tisches.
     */
    public GameController(Stage stage, DataOutputStream out, DataInputStream in, String user, String table_name) {
        this.stage = stage;
        this.out = out;
        this.in = in;
        this.user = user;
        this.table_name = table_name;
        this.chat_messages = new ArrayList<>();

        this.stage.setOnCloseRequest(this::handleWindowClose);
    }

    /**
     * Handhabt das Beenden des Spiels und wechselt zurück zur Lobby.
     *
     * @throws IOException Wenn ein I/O-Fehler auftritt.
     */
    @FXML
    private void quitClick() throws IOException {
        timer.cancel();
        timer.purge();

        out.writeUTF("leavetable:" + table_name + ":" + user);
        out.flush();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lobby-view.fxml"));
        loader.setControllerFactory(param -> new LobbyController(stage, out, in, user));
        Scene scene = new Scene(loader.load());

        stage.setScene(scene);
        stage.setTitle("Lobby");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Initialisiert das Spiel, einschließlich der Setzung der Spieler-Avatare und -Namen
     * sowie der Balance des aktuellen Benutzers.
     *
     * @throws IOException Wenn ein I/O-Fehler auftritt.
     */
    private void initializeGame() throws IOException {

        Collections.addAll(nameLabelList, player1Label, player2Label, player3Label, player4Label, player5Label, player6Label, player7Label, player8Label);
        Collections.addAll(circleList, avatar1, avatar2, avatar3, avatar4, avatar5, avatar6, avatar7, avatar8);

        playerToBetLabelMap = new HashMap<>();
        playerToBetLabelMap.put(player1Label, player1BetLabel);
        playerToBetLabelMap.put(player2Label, player2BetLabel);
        playerToBetLabelMap.put(player3Label, player3BetLabel);
        playerToBetLabelMap.put(player4Label, player4BetLabel);
        playerToBetLabelMap.put(player5Label, player5BetLabel);
        playerToBetLabelMap.put(player6Label, player6BetLabel);
        playerToBetLabelMap.put(player7Label, player7BetLabel);
        playerToBetLabelMap.put(player8Label, player8BetLabel);

        playerToScoreLabelMap = new HashMap<>();
        playerToScoreLabelMap.put(player1Label, player1ScoreLabel);
        playerToScoreLabelMap.put(player2Label, player2ScoreLabel);
        playerToScoreLabelMap.put(player3Label, player3ScoreLabel);
        playerToScoreLabelMap.put(player4Label, player4ScoreLabel);
        playerToScoreLabelMap.put(player5Label, player5ScoreLabel);
        playerToScoreLabelMap.put(player6Label, player6ScoreLabel);
        playerToScoreLabelMap.put(player7Label, player7ScoreLabel);
        playerToScoreLabelMap.put(player8Label, player8ScoreLabel);


        out.writeUTF("initializegame:" + table_name + ":" + user);
        out.flush();

        String info_string = in.readUTF();
        String[] info = info_string.split("@");

        this.balance = Integer.parseInt(info[0]);

        Random random = new Random();
        ArrayList<Integer> tmp = new ArrayList<>();

        for (int i = 1; i < info.length; i++) {
            // Spielernamen setzen
            Label label = nameLabelList.get(i-1);
            label.setText(info[i]);

            // Spieleravatar setzen
            int r = random.nextInt(8);

            while (tmp.contains(r)) {
                r = random.nextInt(8);
            }

            tmp.add(r);


            Circle circle = circleList.get(i-1);
            try {
                Image avatarImage = new Image("images/avatar/avatar" + r + ".png");
                ImagePattern imagePattern = new ImagePattern(avatarImage);
                circle.setFill(imagePattern);

            } catch (IllegalArgumentException e) { // IDE
                Image avatarImage = new Image(System.getProperty("user.dir") + "/src/main/resources/images/avatar/avatar" +  r + ".png");
                ImagePattern imagePattern = new ImagePattern(avatarImage);
                circle.setFill(imagePattern);
            }

            // Balance setzen
            playerBalanceLabel.setText(String.valueOf(balance));
        }
    }

    /**
     * Lädt den aktuellen Spielzustand vom Server neu und aktualisiert die Benutzeroberfläche.
     *
     * @throws IOException Wenn ein I/O-Fehler auftritt.
     */
    private void reloadGameState() throws IOException {
        out.writeUTF("reloadgamestate:" + table_name + ":" + user);
        out.flush();

        String state = in.readUTF();

        if (state.startsWith("countdown")) { // Spiel befindet sich in der "Einsatz setzen" Phase

            // Button state anpassen-------------------------------------------------------------
            StandButton.setDisable(true);
            HitButton.setDisable(true);
            SurrenderButton.setDisable(true);
            SplitButton.setDisable(true);
            DDButton.setDisable(true);
            // -----------------------------------------------------------------------------------

            String[] data = state.split(":");
            Integer countdown = Integer.parseInt(data[1]);
            timerLabel.setText(countdown.toString());

            if (countdown != this.count) {
                this.count = countdown;
            }

            if ((countdown) <= 5) { // In den letzen 5 Sekunden darf auf Grund von out of sync Fehler mit Server nichts mehr gesetzt werden
                stakeField.setDisable(true);
                setStakeButton.setDisable(true);

                // Karten Images löschen
                dealerImageView1.setImage(null);
                dealerImageView2.setImage(null);
                playerImageView1.setImage(null);
                playerImageView2.setImage(null);

                playerSplitImageView1.setImage(null);
                playerSplitImageView2.setImage(null);
                playerSplitVBox1.setVisible(false);
                playerSplitVBox2.setVisible(false);

                // zusätzliche Karten löschen
                for (ImageView i:imageViewArray) {
                    root.getChildren().remove(i);
                }

                for (Label playerLabel : playerToScoreLabelMap.keySet()) {
                    Label l = playerToScoreLabelMap.get(playerLabel);
                    l.setVisible(false);
                }

                resultLabel.setText("");
                winningsLabel.setText("");

            } else if (countdown == 30) {
                stakeField.setDisable(false);
                setStakeButton.setDisable(false);
            }

        } else if (state.startsWith("result")){ // Spiel ist vorbei
            //System.out.println("------------------------------");
            String[] last_info = state.split("#");

            String[] states = last_info[1].split("@");
            try {
                String result = last_info[2];

                // Balance anpassen und resultLabel aktualisieren ---------------------------------------------------------------------------------------------
                //System.out.println("Balance vor Ende: " + playerBalanceLabel.getText());
                //System.out.println("Balance nach Ende: " + result.split(":")[0]);

                if (Integer.parseInt(playerBalanceLabel.getText()) < Integer.parseInt(result.split(":")[0])) {
                    resultLabel.setText("gewonnen");
                    winningsLabel.setText(String.valueOf(Integer.parseInt(result.split(":")[0])-Integer.parseInt(playerBalanceLabel.getText())));
                } else {
                    resultLabel.setText("verloren");
                    winningsLabel.setText(String.valueOf(Integer.parseInt(result.split(":")[0])-Integer.parseInt(playerBalanceLabel.getText())));
                }
                playerBalanceLabel.setText(states[0]);

            } catch (ArrayIndexOutOfBoundsException e) {
                //System.out.println("[Client] Spieler hat passiv an Spiel teilgenommen");
            }


            // -------------------------------------------------------------------------------------------------------------------------------------------

            String[] active_player_info = states[1].split(":");
            String active_player = active_player_info[0];
            String ddable = active_player_info[1];
            String splitable = active_player_info[2];
            String surrenderable = active_player_info[3];

            //System.out.println("Client User: " + user);
            //System.out.println("Aktiver Spieler: " + active_player);
            //System.out.println("DD: " + ddable + ", Split: " + splitable + ", Surrender: " + surrenderable);

            String[] hands = states[2].split("&");
            String[] hand_1 = hands[0].split(":");

            //System.out.println("Hand 1: " + Arrays.toString(hand_1));

            try {
                String[] hand_2 = hands[1].split(":");
                //System.out.println("Hand 2: " + Arrays.toString(hand_2));

            } catch (ArrayIndexOutOfBoundsException e) {
                //System.out.println("[Client] Aktiver Spieler besitzt keine 2. Hand");
            }

            String[] dealer_info = states[3].split("&");
            //String dealer_visibility_second_card = dealer_info[0];
            String[] dealer_cards = dealer_info[1].split(":");

            // Dealer Karten aufdecken ----------------------------------------------------------
            try {
                String path = "/images/cards/";
                dealerImageView2.setImage(new Image(path + dealer_cards[1] + ".png"));
                if (dealer_cards.length > 2) {
                    for (int i=2; i<=dealer_cards.length-1; i++) {
                        ImageView newImageView = new ImageView();
                        newImageView.setLayoutX(840);
                        newImageView.setLayoutY(50 + (i-1)*20);
                        newImageView.setFitWidth(65);
                        newImageView.setFitHeight(100);
                        newImageView.setImage(new Image(path + dealer_cards[i] + ".png"));
                        imageViewArray.add(newImageView);
                        root.getChildren().add(newImageView);
                    }
                }
                //  DD anzeigen---------------------------------------------------------------------
                if (hand_1.length > 2) {
                    for (int i=2; i<=hand_1.length-1; i++) {
                        ImageView newImageView = new ImageView();
                        newImageView.setLayoutX(840);
                        newImageView.setLayoutY(240 + (i-1)*20);
                        newImageView.setFitWidth(65);
                        newImageView.setFitHeight(100);
                        newImageView.setImage(new Image(path + hand_1[i] + ".png"));
                        imageViewArray.add(newImageView);
                        root.getChildren().add(newImageView);
                    }
                }

            } catch (IllegalArgumentException e) { // IDE
                String path = System.getProperty("user.dir") + "/src/main/resources/images/cards/";

                dealerImageView2.setImage(new Image(path + dealer_cards[1]+".png"));
                if (dealer_cards.length > 2) {
                    for (int i=2; i<=dealer_cards.length-1; i++) {
                        ImageView newImageView = new ImageView();
                        newImageView.setLayoutX(840);
                        newImageView.setLayoutY(50 + (i-1)*20);
                        newImageView.setFitWidth(65);
                        newImageView.setFitHeight(100);
                        newImageView.setImage(new Image(path + dealer_cards[i] + ".png"));
                        imageViewArray.add(newImageView);
                        root.getChildren().add(newImageView);
                    }
                }
                //  DD anzeigen---------------------------------------------------------------------
                if (hand_1.length > 2) {
                    for (int i=2; i<=hand_1.length-1; i++) {
                        ImageView newImageView = new ImageView();
                        newImageView.setLayoutX(840);
                        newImageView.setLayoutY(240 + (i-1)*20);
                        newImageView.setFitWidth(65);
                        newImageView.setFitHeight(100);
                        newImageView.setImage(new Image(path + hand_1[i] + ".png"));
                        imageViewArray.add(newImageView);
                        root.getChildren().add(newImageView);
                    }
                }
            }

            //----------------------------------------------------------------------------------

            HashMap<String, String> stakes = new HashMap<>();

            for (String s : states[4].split(":")) {
                String[] stake_info = s.split("&");
                String user_name = stake_info[0];
                String stake = stake_info[1];

                stakes.put(user_name, stake);
            }

            for (Label playerLabel : playerToBetLabelMap.keySet()) {
                    Label l = playerToBetLabelMap.get(playerLabel);
                    l.setVisible(false);
            }

            // Button state anpassen-------------------------------------------
            StandButton.setDisable(true);
            HitButton.setDisable(true);
            SurrenderButton.setDisable(true);
            SplitButton.setDisable(true);
            DDButton.setDisable(true);

            stakeField.setDisable(false);
            setStakeButton.setDisable(false);

            // ------------------------------------------------------------------

        } else { // Spiel ist im Gange

            String[] states = state.split("@");

            String[] active_player_info = states[1].split(":");
            String active_player = active_player_info[0];
            String ddable = active_player_info[1];
            String splitable = active_player_info[2];
            String surrenderable = active_player_info[3];

            //System.out.println("Client User: " + user);
            //System.out.println("Aktiver Spieler: " + active_player);
            //System.out.println("DD: " + ddable + ", Split: " + splitable + ", Surrender: " + surrenderable);

            for (Label l : nameLabelList) {
                if (l.getParent() instanceof VBox) {
                    VBox parent = (VBox) l.getParent();
                    Color strokeColor = Objects.equals(l.getText(), active_player) ? Color.GREEN : Color.WHITE;
                    for (Node child : parent.getChildren()) {
                        if (child instanceof Circle) {
                            ((Circle) child).setStroke(strokeColor);
                        }
                    }
                }
            }


            // Spieler Karte anzeigen-----------------------------------------------------------------
            String[] hands = states[2].split("&");
            String[] hand_1 = hands[0].split(":");

            try {
                String path = "/images/cards/";

                try {
                    playerImageView1.setImage(new Image(path + hand_1[0] + ".png"));
                    playerImageView2.setImage(new Image(path + hand_1[1] + ".png"));

                    if (hand_1.length > 2) {
                        for (int i=2; i<=hand_1.length-1; i++) {
                            ImageView newImageView = new ImageView();
                            newImageView.setLayoutX(840);
                            newImageView.setLayoutY(240 + (i-1)*20);
                            newImageView.setFitWidth(65);
                            newImageView.setFitHeight(100);
                            newImageView.setImage(new Image(path + hand_1[i] + ".png"));
                            imageViewArray.add(newImageView);
                            root.getChildren().add(newImageView);
                        }
                    }

                    try {
                        String[] hand_2 = hands[1].split(":");
                        //System.out.println("Hand 2: " + Arrays.toString(hand_2));

                        playerSplitVBox1.setVisible(true);
                        playerSplitVBox2.setVisible(true);

                        playerSplitImageView1.setImage(new Image(path + hand_1[0] + ".png"));
                        playerImageView1.setImage(new Image(path + hand_1[1] + ".png"));
                        playerImageView2.setImage(new Image(path + hand_2[0] + ".png"));
                        playerSplitImageView2.setImage(new Image(path + hand_2[1] + ".png"));

                    } catch (ArrayIndexOutOfBoundsException e) {
                        //System.out.println("[Client] Aktiver Spieler besitzt keine 2. Hand");
                    }
                } catch (Exception ex) {
                    //System.out.println("Spieler nimmt nicht aktiv teil");
                }

                // Dealer Karte anzeigen -----------------------------------------------------------------------
                String[] dealer_info = states[3].split("&");
                String[] dealer_cards = dealer_info[1].split(":");

                dealerImageView1.setImage(new Image(path + dealer_cards[0] + ".png"));
                dealerImageView2.setImage(new Image(path + "back.png"));


            } catch (IllegalArgumentException e) { // IDE
                String path = System.getProperty("user.dir") + "/src/main/resources/images/cards/";

                try {
                    playerImageView1.setImage(new Image(path+hand_1[0]+".png"));
                    playerImageView2.setImage(new Image(path+hand_1[1]+".png"));

                    if (hand_1.length > 2) {
                        for (int i=2; i<=hand_1.length-1; i++) {
                            ImageView newImageView = new ImageView();
                            newImageView.setLayoutX(840);
                            newImageView.setLayoutY(240 + (i-1)*20);
                            newImageView.setFitWidth(65);
                            newImageView.setFitHeight(100);
                            newImageView.setImage(new Image(path+hand_1[i]+".png"));
                            imageViewArray.add(newImageView);
                            root.getChildren().add(newImageView);
                        }
                    }

                    try {
                        String[] hand_2 = hands[1].split(":");
                        //System.out.println("Hand 2: " + Arrays.toString(hand_2));

                        playerSplitVBox1.setVisible(true);
                        playerSplitVBox2.setVisible(true);

                        playerSplitImageView1.setImage(new Image(path+hand_1[0]+".png"));
                        playerImageView1.setImage(new Image(path+hand_1[1]+".png"));
                        playerImageView2.setImage(new Image(path+hand_2[0]+".png"));
                        playerSplitImageView2.setImage(new Image(path+hand_2[1]+".png"));

                    } catch (ArrayIndexOutOfBoundsException ex) {
                        //System.out.println("[Client] Aktiver Spieler besitzt keine 2. Hand");
                    }

                } catch (Exception ex) {
                    //System.out.println("Spieler nimmt nicht aktiv teil");
                }


                // Dealer Karte anzeigen -----------------------------------------------------------------------
                String[] dealer_info = states[3].split("&");
                String[] dealer_cards = dealer_info[1].split(":");

                dealerImageView1.setImage(new Image(path+dealer_cards[0]+".png"));
                dealerImageView2.setImage(new Image(path+"back.png"));
            }

            // -----------------------------------------------------------------------------------------------
            HashMap<String, String> stakes = new HashMap<>();

            for (String s : states[4].split(":")) {
                String[] stake_info = s.split("&");
                String user_name = stake_info[0];
                String stake = stake_info[1];

                //System.out.println("Einsatz von: " + user_name + ": " + stake);
                for (Label playerLabel : playerToBetLabelMap.keySet()) {
                    if (playerLabel.getText().equals(user_name)) {
                        Label l = playerToBetLabelMap.get(playerLabel);
                        l.setText(stake);
                        l.setVisible(true);
                    }
                }
                stakes.put(user_name, stake);
            }

            out.writeUTF("getallscores:" + table_name);
            String allscores = in.readUTF();
            //System.out.println("Alle Scores: " + allscores);

            for (String s : allscores.split(":")) {
                String[] info = s.split("/");
                String user_name_score = info[0];
                String hand_1_score = info[1];
                String hand_2_score = info[2];

                for (Label playerLabel : playerToScoreLabelMap.keySet()) {
                    if (playerLabel.getText().equals(user_name_score)) {
                        Label l = playerToScoreLabelMap.get(playerLabel);
                        if (hand_2_score.equals("0")) {
                            l.setText(hand_1_score);
                        } else {
                            l.setText(hand_1_score+" / "+hand_2_score);
                        }
                        l.setVisible(true);
                    }
                }

            }

            // Button state anpassen -----------------------------------------------------------
            if (!active_player.equals(user)) {
                StandButton.setDisable(true);
                HitButton.setDisable(true);
                SurrenderButton.setDisable(true);
                SplitButton.setDisable(true);
                DDButton.setDisable(true);

            } else {
                StandButton.setDisable(false);
                HitButton.setDisable(false);
                SurrenderButton.setDisable(!Boolean.parseBoolean(surrenderable));
                SplitButton.setDisable(!Boolean.parseBoolean(splitable));
                DDButton.setDisable(!Boolean.parseBoolean(ddable));
            }
        }
    }

    /**
     * Setzt den Einsatz für das aktuelle Spiel, wenn der eingegebene Betrag gültig ist.
     * Wenn der Einsatz kleiner oder gleich dem verfügbaren Guthaben ist, wird der Einsatz an den Server gesendet.
     * Andernfalls wird eine Fehlermeldung in der Konsole ausgegeben.
     *
     * @param actionEvent Das ActionEvent, das dieses Ereignis ausgelöst hat.
     * @throws IOException Wenn ein I/O-Fehler auftritt.
     */
    public void setStake(ActionEvent actionEvent) throws IOException {
       if (!stakeField.getText().isEmpty()) {
           if (Integer.parseInt(stakeField.getText()) <= balance) {
               out.writeUTF("setstake:" + table_name + ":" + user + ":" + stakeField.getText());
               out.flush();

               stakeField.setDisable(true);
               setStakeButton.setDisable(true);

           } else {
               System.out.println("[Client] Einsatz größer als verfügbares Guthaben");
           }

       } else {
           System.out.println("[Client] Einsatz darf nicht leer sein");
       }
    }

    /**
     * Sendet eine Anfrage an den Server, um eine Karte zu ziehen.
     * Dies wird verwendet, wenn der Benutzer den "Hit"-Button drückt.
     *
     * @param actionEvent Das ActionEvent, das dieses Ereignis ausgelöst hat.
     * @throws IOException Wenn ein I/O-Fehler auftritt.
     */
    public void hitClick(ActionEvent actionEvent) throws IOException {
       out.writeUTF("hit:" + table_name);
       out.flush();
    }

    /**
     * Sendet eine Anfrage an den Server, um den aktuellen Zug abzuschließen.
     * Dies wird verwendet, wenn der Benutzer den "Stand"-Button drückt.
     *
     * @param actionEvent Das ActionEvent, das dieses Ereignis ausgelöst hat.
     * @throws IOException Wenn ein I/O-Fehler auftritt.
     */
    public void standClick(ActionEvent actionEvent) throws IOException {
        out.writeUTF("stand:" + table_name);
        out.flush();
    }

    /**
     * Sendet eine Anfrage an den Server, um einen Double Down durchzuführen.
     * Dies wird verwendet, wenn der Benutzer den "Double Down"-Button drückt.
     *
     * @param actionEvent Das ActionEvent, das dieses Ereignis ausgelöst hat.
     * @throws IOException Wenn ein I/O-Fehler auftritt.
     */
    public void DDClick(ActionEvent actionEvent) throws IOException {
        out.writeUTF("doubledown:" + table_name);
        out.flush();
    }

    /**
     * Sendet eine Anfrage an den Server, um eine Split-Aktion durchzuführen.
     * Dies wird verwendet, wenn der Benutzer den "Split"-Button drückt.
     *
     * @param actionEvent Das ActionEvent, das dieses Ereignis ausgelöst hat.
     * @throws IOException Wenn ein I/O-Fehler auftritt.
     */
    public void splitClick(ActionEvent actionEvent) throws IOException {
        out.writeUTF("split:" + table_name);
        out.flush();
    }

    /**
     * Sendet eine Anfrage an den Server, um aufzugeben.
     * Dies wird verwendet, wenn der Benutzer den "Surrender"-Button drückt.
     *
     * @param actionEvent Das ActionEvent, das dieses Ereignis ausgelöst hat.
     * @throws IOException Wenn ein I/O-Fehler auftritt.
     */
    public void surrenderClick(ActionEvent actionEvent) throws IOException {
        out.writeUTF("surrender:" + table_name);
        out.flush();
    }

    /**
     * Behandelt das Senden einer Chat-Nachricht.
     * @param keyEvent Das KeyEvent, das das Drücken der Enter-Taste auslöst.
     * @throws IOException Falls ein Fehler beim Senden der Nachricht auftritt.
     */
    @FXML
    private void sendChatMessage(KeyEvent keyEvent) throws IOException {
        if ((keyEvent.getCode() == KeyCode.ENTER) && !(chatField.getText().isBlank())) {
            String message = chatField.getText();
            chatField.clear();

            out.writeUTF("chatmessage:" + this.user + ":" + message);
            out.flush();
        }
    }

    /**
     * Lädt die Chat-Nachrichten neu vom Server und aktualisiert die Ansicht.
     * @throws IOException Falls ein Fehler bei der Kommunikation mit dem Server auftritt.
     */
    private void reloadChatMessages() throws IOException {
        out.writeUTF("reloadchat");
        out.flush();
        String up_to_date_messages = in.readUTF();

        if (!up_to_date_messages.equals("")) {
            ArrayList<String> up_to_date_messages_arr = new ArrayList<>(Arrays.asList(up_to_date_messages.split("@")));

            // Ersetze User durch "Me" für CSS-Styling
            for (int i = 0; i < up_to_date_messages_arr.size(); i++) {
                String message = up_to_date_messages_arr.get(i);
                if (message.contains(user)) {
                    up_to_date_messages_arr.set(i, message.replace(user, "Me"));
                }
            }

            if (!up_to_date_messages_arr.equals(chat_messages)) {
                int index = chat_messages.size();

                for (int i = index; i < up_to_date_messages_arr.size(); i++) {
                    chatView.getItems().add(up_to_date_messages_arr.get(i));
                }

                chatView.scrollTo(up_to_date_messages_arr.size() - 1);
                chat_messages = up_to_date_messages_arr;
            }
        }
    }

    @FXML
    public void infoClick(ActionEvent actionEvent) {
        String url = "https://ts-spielkarten.de/spielregeln-kartenspiele/black-jack-spielregeln/";
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Desktop is not supported. Can't open the browser.");
        }
    }

    /**
     * Handhabt das Schließen des Fensters, indem es die Verbindung zum Server trennt.
     *
     * @param windowEvent Das WindowEvent beim Schließen des Fensters.
     */
    private void handleWindowClose(WindowEvent windowEvent) {
        try {
            out.writeUTF("leavetable:" + table_name + ":" + user);
            out.flush();

            out.writeUTF("logout:" + user); // Logout an den Server senden
            out.flush();

            timer.cancel();
            timer.purge();

            out.close();
            in.close();

            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}

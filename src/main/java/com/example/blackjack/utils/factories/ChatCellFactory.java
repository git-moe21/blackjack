package com.example.blackjack.utils.factories;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import javafx.scene.paint.Color;

/**
 * Eine Factory-Klasse zur Erstellung von benutzerdefinierten ListCell-Objekten für Chatnachrichten.
 */
public class ChatCellFactory implements Callback<ListView<String>, ListCell<String>> {

    /**
     * Erzeugt eine neue Instanz einer benutzerdefinierten ListCell für eine ListView.
     *
     * @param param Die ListView, für die die Zelle erstellt wird
     * @return Eine neue Instanz von ChatListCell
     */
    @Override
    public ListCell<String> call(ListView<String> param) {
        return new ChatListCell();
    }

    /**
     * Eine benutzerdefinierte ListCell-Klasse für die Darstellung von Chatnachrichten.
     */
    private static class ChatListCell extends ListCell<String> {
        private HBox messageContainer; // Container für die Nachricht
        private TextFlow messageText; // TextFlow für die Nachricht

        /**
         * Konstruktor für ChatListCell.
         * Initialisiert die Benutzeroberfläche für die Chatnachricht.
         */
        public ChatListCell() {
            messageText = new TextFlow();
            messageText.setMaxWidth(350);
            messageContainer = new HBox(messageText);
            messageContainer.setSpacing(1);
            setGraphic(messageContainer);
        }

        /**
         * Aktualisiert den Inhalt der Zelle.
         *
         * @param item Der neue Inhalt für die Zelle
         * @param empty Gibt an, ob die Zelle leer ist
         */
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                Text message = new Text(item);
                message.setFill(Color.WHITE);
                message.setFont(Font.font("System", 14));

                messageText.getChildren().clear();
                messageText.getChildren().add(message);

                messageContainer.getStyleClass().add("list-cell");
                if (item.startsWith("Me:")) {
                    messageContainer.getStyleClass().add("me");
                } else {
                    messageContainer.getStyleClass().add("other");
                }
                setGraphic(messageContainer);
            }
        }
    }
}

package com.example.blackjack.utils.game.exceptions;

/**
 * Die Ausnahme, bei der es um fehlerhafte Spielzüge geht.
 * Diese können entweder zu einem falschen Zeitpunkt betätigt werden,
 * oder der Spielzug ist in der aktuellen Situation unzulässig.
 * Dazu gehören auch Fehler mit dem Guthaben eines Spielers.
 */
public class InvalidMoveException extends Exception {
    public InvalidMoveException(String s) {
    }
}

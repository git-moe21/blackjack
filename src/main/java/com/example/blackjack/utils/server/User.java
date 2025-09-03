package com.example.blackjack.utils.server;

import java.io.Serializable;

/**
 * Die User-Klasse repräsentiert einen Benutzer mit Benutzername und Passwort.
 * Sie implementiert das Serializable-Interface, um Objekte serialisierbar zu machen.
 */
public class User implements Serializable {
    private String username;
    private String password;

    /**
     * Konstruktor für die User-Klasse.
     *
     * @param username der Benutzername des Benutzers
     * @param password das Passwort des Benutzers
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Gibt den Benutzernamen des Benutzers zurück.
     *
     * @return der Benutzername
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gibt das Passwort des Benutzers zurück.
     *
     * @return das Passwort
     */
    public String getPassword() {
        return password;
    }
}

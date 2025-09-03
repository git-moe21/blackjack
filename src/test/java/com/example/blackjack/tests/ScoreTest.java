package com.example.blackjack.tests;

import com.example.blackjack.utils.game.Score;
import com.example.blackjack.utils.game.player.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ScoreTest {
    private Score score;
    private Player player;

    @Before
    public void setUp() {
        player = new Player("tester", 1000);
        score = new Score(player.getUsername(), 20);
    }

    @Test
    public void testGetterAndSetters() {
        assertEquals(20, score.getScore());
        assertEquals("tester", score.getUser());
        score.setScore(10);
        assertEquals(10, score.getScore());
    }
}
package com.tysiac.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Player {

    // --- TUTAJ DODALIŚMY WALIDACJĘ ---
    @NotNull(message = "Nick nie może być nullem")
    @NotBlank(message = "Nick gracza nie może być pusty")
    @Size(min = 2, max = 20, message = "Nick musi mieć od 2 do 20 znaków")
    private final String name;
    // ---------------------------------

    private final List<Card> hand;
    private int score;
    private int roundScore;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.score = 0;
        this.roundScore = 0;
    }

    public void receiveCard(Card card) {
        hand.add(card);
        sortHand(); // Sortujemy od razu przy dodaniu
    }

    // --- SORTOWANIE (Serce -> Pik, As -> 9) ---
    public void sortHand() {
        hand.sort(Comparator
                .comparing(Card::suit) // Najpierw kolor (według kolejności w Enumie Suit)
                .thenComparing(Card::rank) // Potem figura (według kolejności w Enumie Rank)
        );
    }

    public void addRoundPoints(int points) { this.roundScore += points; }
    public void resetRoundScore() { this.roundScore = 0; }

    public String getName() { return name; }
    public List<Card> getHand() { return hand; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getRoundScore() { return roundScore; }
    public void setRoundScore(int roundScore) { this.roundScore = roundScore; }
}
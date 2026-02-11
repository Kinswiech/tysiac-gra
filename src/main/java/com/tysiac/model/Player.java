package com.tysiac.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//klasa do gracza
public class Player {

    //Walidacja
    @NotNull(message = "Nick nie może być nullem")
    @NotBlank(message = "Nick gracza nie może być pusty")
    @Size(min = 2, max = 20, message = "Nick musi mieć od 2 do 20 znaków")
    private final String name;

    //stan gracza
    private final List<Card> hand;
    private int score;
    private int roundScore;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.score = 0;
        this.roundScore = 0;
    }

    //metoda do rozdawania kart do ręki
    public void receiveCard(Card card) {
        hand.add(card);
        sortHand();
    }

    //sortowanie kart kolorem i wielkością
    public void sortHand() {
        hand.sort(Comparator
                .comparing(Card::suit)
                .thenComparing(Card::rank)
        );
    }

    //dodanie punktów za lewę lub meldunek
    public void addRoundPoints(int points) { this.roundScore += points; }
    //zerowanie punktów rundy
    public void resetRoundScore() { this.roundScore = 0; }

    //gettery i settery
    public String getName() { return name; }
    public List<Card> getHand() { return hand; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getRoundScore() { return roundScore; }
    public void setRoundScore(int roundScore) { this.roundScore = roundScore; }
}
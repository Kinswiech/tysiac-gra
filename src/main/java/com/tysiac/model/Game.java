package com.tysiac.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Game {
    // Gettery/Settery (Standardowe)
    @Getter
    private final List<Player> players;
    private Deck deck;
    @Getter
    private final List<Card> musik;
    @Getter
    private final List<Card> table;

    @Setter
    @Getter
    private int currentPlayerIndex;
    @Setter
    @Getter
    private Suit trumpSuit;

    @Setter
    @Getter
    private GamePhase phase;
    @Setter
    @Getter
    private int currentBid;
    @Setter
    @Getter
    private int highestBidderIndex;
    @Getter
    private int passCount;
    @Getter
    private int cardsGivenCount;

    public Game() {
        this.players = new ArrayList<>(); // Pusta lista na start!
        this.musik = new ArrayList<>();
        this.table = new ArrayList<>();
        this.phase = GamePhase.BIDDING; // Domyślnie
    }

    // Metoda dla Service: Dodaj gracza
    public void addPlayer(String name) {
        if (players.size() >= 3) throw new IllegalStateException("Stół jest pełny!");
        if (players.stream().anyMatch(p -> p.getName().equals(name))) {
            throw new IllegalArgumentException("Nick zajęty!");
        }
        players.add(new Player(name));
    }

    public boolean isReadyToStart() {
        return players.size() == 3;
    }

    public void startNewRound() {
        this.deck = new Deck(); // NOWY deck (reset kart)
        this.deck.shuffle();

        musik.clear();
        table.clear();
        players.forEach(p -> {
            p.getHand().clear();
            p.resetRoundScore();
        });

        // Rozdajemy karty
        for (Player player : players) {
            for (int i = 0; i < 7; i++) {
                player.receiveCard(deck.dealCard());
            }
        }
        while(deck.size() > 0) musik.add(deck.dealCard());

        // Reset zmiennych rundy
        // Ważne: Zwycięzca poprzedniej partii powinien zaczynać (na razie upraszczamy: Gracz 0)
        // W pełnej wersji: rotacja dealera.
        currentPlayerIndex = 0;
        highestBidderIndex = 0;
        currentBid = 100;
        passCount = 0;
        cardsGivenCount = 0;
        phase = GamePhase.BIDDING;
        trumpSuit = null;
    }

    public void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % 3;
    }

    public void incrementPassCount() { this.passCount++; }
    public void resetPassCount() { this.passCount = 0; }

    public void incrementCardsGivenCount() { this.cardsGivenCount++; }
}
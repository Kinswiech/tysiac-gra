package com.tysiac.model;

import java.util.ArrayList;
import java.util.List;

public class Game {
    // --- POLA (Czyste, bez adnotacji) ---
    private final List<Player> players;
    private Deck deck;
    private final List<Card> musik;
    private final List<Card> table;

    private int currentPlayerIndex;
    private Suit trumpSuit;
    private GamePhase phase;
    private int currentBid;
    private int highestBidderIndex;
    private int passCount;
    private int cardsGivenCount;
    private int roundStarterIndex = -1;

    // --- KONSTRUKTOR ---
    public Game() {
        this.players = new ArrayList<>();
        this.musik = new ArrayList<>();
        this.table = new ArrayList<>();
        this.phase = GamePhase.BIDDING;
    }

    // --- LOGIKA GRY ---

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
        this.deck = new Deck();
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

        // Rotacja rozdającego
        roundStarterIndex = (roundStarterIndex + 1) % 3;
        currentPlayerIndex = roundStarterIndex;

        highestBidderIndex = currentPlayerIndex;
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

    // --- GETTERY I SETTERY (Ręczne - to co robił Lombok, ale pewne) ---

    public List<Player> getPlayers() {
        return players;
    }

    public List<Card> getMusik() {
        return musik;
    }

    public List<Card> getTable() {
        return table;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public Suit getTrumpSuit() {
        return trumpSuit;
    }

    public void setTrumpSuit(Suit trumpSuit) {
        this.trumpSuit = trumpSuit;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public int getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(int currentBid) {
        this.currentBid = currentBid;
    }

    public int getHighestBidderIndex() {
        return highestBidderIndex;
    }

    public void setHighestBidderIndex(int highestBidderIndex) {
        this.highestBidderIndex = highestBidderIndex;
    }

    public int getPassCount() {
        return passCount;
    }

    public int getCardsGivenCount() {
        return cardsGivenCount;
    }

    public int getRoundStarterIndex() {
        return roundStarterIndex;
    }
}
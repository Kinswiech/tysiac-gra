package com.tysiac.model;

// Zero importów z Lomboka!

public class Card {
    private final Rank rank;
    private final Suit suit;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    // --- TE METODY ZASTĘPUJĄ LOMBOKA (Dla Reacta/JSON) ---
    // Bez nich React nie zobaczy, jaka to karta!
    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    // --- TE METODY SĄ DLA GameService (Żeby kod się nie sypał) ---
    public Rank rank() {
        return rank;
    }

    public Suit suit() {
        return suit;
    }

    public int getPoints() {
        switch (rank) {
            case ACE: return 11;
            case TEN: return 10;
            case KING: return 4;
            case QUEEN: return 3;
            case JACK: return 2;
            default: return 0;
        }
    }

    @Override
    public String toString() {
        return rank + " " + suit;
    }
}
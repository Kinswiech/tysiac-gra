package com.tysiac.model;

import lombok.Getter;

@Getter
public class Card {
    // --- TE METODY SĄ DLA REACTA (JSON) ---
    private final Rank rank;
    private final Suit suit;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
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

    // --- TE METODY SĄ DLA GameService (Naprawiają Twój błąd) ---
    public Rank rank() {
        return rank;
    }

    public Suit suit() {
        return suit;
    }

    @Override
    public String toString() {
        return rank + " " + suit;
    }
}
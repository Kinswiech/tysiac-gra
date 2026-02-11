package com.tysiac.model;

//klasa karta
public class Card {
    private final Rank rank;
    private final Suit suit;

    //kostruktor tworzenia nowej karty
    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank rank() {
        return rank;
    }

    public Suit suit() {
        return suit;
    }

    //metoda do zwracania liczby punktów do każdej karty
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
package com.tysiac.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards = new ArrayList<>();

    //klasa do talii kart
    public Deck() {
        //Generujemy talię tylko dla Tysiąca (9, J, Q, K, 10, A)
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    //metoda do tasowania
    public void shuffle() {
        Collections.shuffle(cards);
    }

    //Rozdaj jedną kartę
    public Card dealCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Talia jest pusta!");
        }
        return cards.remove(cards.size() - 1); //Usuwamy ostatnią (górną)
    }

    //ile kart zostało w talii
    public int size() {
        return cards.size();
    }
}
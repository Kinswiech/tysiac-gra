package com.tysiac.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards = new ArrayList<>();

    public Deck() {
        // Generujemy talię tylko dla Tysiąca (9, J, Q, K, 10, A)
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    // Rozdaj jedną kartę (zdejmij z góry)
    public Card dealCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Talia jest pusta!");
        }
        return cards.remove(cards.size() - 1); // Usuwamy ostatnią (górną)
    }

    public int size() {
        return cards.size();
    }
}
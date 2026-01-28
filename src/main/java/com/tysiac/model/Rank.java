package com.tysiac.model;

public enum Rank {
    ACE(11),
    TEN(10),
    KING(4),
    QUEEN(3),
    JACK(2),
    NINE(0);

    private final int points;

    Rank(int points) {
        this.points = points;
    }

    // --- RĘCZNY GETTER (Zamiast Lomboka) ---
    public int getPoints() {
        return points;
    }
}
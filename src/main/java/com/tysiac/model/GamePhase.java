package com.tysiac.model;

public enum GamePhase {
    BIDDING, // Licytacja
    SHARING,
    DECLARING,// Nowy etap: Rozdawanie nadmiarowych kart
    PLAYING,
    GAME_OVER
}
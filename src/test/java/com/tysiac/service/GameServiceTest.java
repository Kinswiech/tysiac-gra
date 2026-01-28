package com.tysiac.service;

import com.tysiac.model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService gameService;

    @BeforeEach
    void setUp() {
        // Przed każdym testem tworzymy czystą instancję serwisu
        gameService = new GameService();
    }

    @Test
    void shouldStartGameWhenThirdPlayerJoins() {
        // Given (Mamy 3 graczy)
        gameService.addPlayer("Kinga");
        gameService.addPlayer("Mikołaj");

        // When (Dodajemy trzeciego)
        gameService.addPlayer("Bot");
        Game gameState = gameService.getGame();

        // Then (Gra powinna wystartować)
        assertEquals(3, gameState.getPlayers().size(), "Powinno być 3 graczy");
        // Sprawdzamy czy każdy dostał 7 kart (bo 3 idą do musika)
        assertEquals(7, gameState.getPlayers().get(0).getHand().size(), "Gracz powinien mieć 7 kart");
    }

    @Test
    void shouldThrowExceptionWhenBidIsTooLow() {
        // Given (Gra wystartowała)
        gameService.addPlayer("P1");
        gameService.addPlayer("P2");
        gameService.addPlayer("P3");

        // When & Then (Próba licytacji poniżej 100 powinna rzucić błąd)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.bid(90); // Za mało, minimum to 100 lub currentBid
        });

        assertNotNull(exception);
    }
}
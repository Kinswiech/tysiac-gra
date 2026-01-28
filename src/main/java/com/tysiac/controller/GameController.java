package com.tysiac.controller;

import com.tysiac.model.Game;
import com.tysiac.service.GameService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/join")
    public Game joinGame(@RequestParam String playerName) {
        gameService.joinGame(playerName);
        return gameService.getGame();
    }

    @GetMapping("/state")
    public Game getGameState() {
        return gameService.getGame();
    }

    @PostMapping("/play")
    public Game playCard(@RequestParam String rank, @RequestParam String suit) {
        gameService.playCard(rank, suit);
        return gameService.getGame();
    }

    @PostMapping("/bid")
    public Game bid(@RequestParam int amount) {
        gameService.bid(amount);
        return gameService.getGame();
    }

    @PostMapping("/share")
    public Game shareCard(@RequestParam String rank, @RequestParam String suit, @RequestParam String targetPlayer) {
        gameService.shareCard(rank, suit, targetPlayer);
        return gameService.getGame();
    }

    // --- NOWY ENDPOINT ---
    @PostMapping("/declare")
    public Game declareBid(@RequestParam int points) {
        gameService.declareBid(points);
        return gameService.getGame();
    }
    @PostMapping("/reset")
    public void resetGame() {
        gameService.resetGame();
    }
}
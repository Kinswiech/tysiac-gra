package com.tysiac.controller;

import com.tysiac.model.Game;
import com.tysiac.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    //Pobieranie stanu
    @GetMapping("/state")
    public Game getGameState() {
        return gameService.getGame();
    }

    //reset gry
    @PostMapping("/reset")
    public void resetGame() {
        System.out.println("--- RESET GRY ---");
        gameService.resetGame();
    }

    //dołączenie gracza do gry
    @PostMapping("/join")
    public ResponseEntity<?> joinGame(@RequestParam String playerName) {
        try {
            gameService.joinGame(playerName);
            return ResponseEntity.ok(gameService.getGame());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    //rzucenie karty
    @PostMapping("/play")
    public ResponseEntity<?> playCard(@RequestParam String rank, @RequestParam String suit) {
        try {
            gameService.playCard(rank, suit);
            return ResponseEntity.ok(gameService.getGame());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    //Obsługa zgłoszania stawki lub pasowanie
    @PostMapping("/bid")
    public ResponseEntity<?> bid(@RequestParam int amount) {
        try {
            gameService.bid(amount);
            return ResponseEntity.ok(gameService.getGame());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    //oddawanie kart z musika
    @PostMapping("/share")
    public ResponseEntity<?> shareCard(@RequestParam String rank, @RequestParam String suit, @RequestParam String targetPlayer) {
        try {
            gameService.shareCard(rank, suit, targetPlayer);
            return ResponseEntity.ok(gameService.getGame());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    //ustalanie ostatecznej deklaracji punktów
    @PostMapping("/declare")
    public ResponseEntity<?> declareBid(@RequestParam int points) {
        try {
            gameService.declareBid(points);
            return ResponseEntity.ok(gameService.getGame());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
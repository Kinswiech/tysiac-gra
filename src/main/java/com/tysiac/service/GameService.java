package com.tysiac.service;

import com.tysiac.model.*;
import lombok.Getter;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Getter
public class GameService {

    private final Game game = new Game();

    // --- LOBBY ---
    public void joinGame(String playerName) {
        if (game.isReadyToStart()) {
            boolean exists = game.getPlayers().stream().anyMatch(p -> p.getName().equals(playerName));
            if (!exists) throw new IllegalStateException("Gra jest w toku. Stół pełny.");
            return;
        }
        game.addPlayer(playerName);
        if (game.isReadyToStart()) {
            game.startNewRound();
            System.out.println("3 graczy zebranych. START!");
        }
    }

    // --- LICYTACJA (Z WALIDACJĄ MAX) ---
    public void bid(int amount) {
        if (game.getPhase() != GamePhase.BIDDING) throw new IllegalStateException("To nie licytacja!");

        if (amount == 0) { // PAS
            game.incrementPassCount();
            if (game.getPassCount() == 2) {
                int winnerIndex = (game.getCurrentPlayerIndex() + 1) % 3;
                game.setHighestBidderIndex(winnerIndex);

                game.setPhase(GamePhase.SHARING);
                game.setCurrentPlayerIndex(winnerIndex);

                Player winner = game.getPlayers().get(winnerIndex);
                winner.getHand().addAll(game.getMusik());
                winner.sortHand();
                game.getMusik().clear();
            } else {
                game.advanceTurn();
            }
        } else { // PRZEBICIE
            // 1. Sprawdź czy przebija obecną stawkę
            if (amount <= game.getCurrentBid()) {
                throw new IllegalArgumentException("Musisz dać więcej niż " + game.getCurrentBid());
            }

            // 2. NOWOŚĆ: Sprawdź czy ma tyle w kartach (Limit = 120 + Pary w ręku)
            Player currentPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());
            int maxPossible = calculateMaxPossibleScore(currentPlayer);

            if (amount > maxPossible) {
                throw new IllegalArgumentException("Nie możesz licytować " + amount + "! Twój max (120 + meldunki w ręku) to: " + maxPossible);
            }

            game.setCurrentBid(amount);
            game.setHighestBidderIndex(game.getCurrentPlayerIndex());
            game.resetPassCount();
            game.advanceTurn();
        }
    }

    // --- METODA POMOCNICZA DO OBLICZANIA MAX STAWKI ---
    private int calculateMaxPossibleScore(Player player) {
        int maxScore = 120; // Zawsze zakładamy, że zgarnie wszystkie lewy

        // Sprawdzamy jakie pary (K+Q) gracz ma FIZYCZNIE w ręku
        if (hasMarriage(player, Suit.HEARTS)) maxScore += 100;
        if (hasMarriage(player, Suit.DIAMONDS)) maxScore += 80;
        if (hasMarriage(player, Suit.CLUBS)) maxScore += 60;
        if (hasMarriage(player, Suit.SPADES)) maxScore += 40;

        return maxScore;
    }

    private boolean hasMarriage(Player player, Suit suit) {
        boolean hasKing = player.getHand().stream().anyMatch(c -> c.suit() == suit && c.rank() == Rank.KING);
        boolean hasQueen = player.getHand().stream().anyMatch(c -> c.suit() == suit && c.rank() == Rank.QUEEN);
        return hasKing && hasQueen;
    }
    // --------------------------------------------------

    // --- ODDAWANIE KART ---
    public void shareCard(String rank, String suit, String targetPlayerName) {
        if (game.getPhase() != GamePhase.SHARING) throw new IllegalStateException("To nie czas na oddawanie!");

        Player winner = game.getPlayers().get(game.getHighestBidderIndex());

        Card cardToGive = winner.getHand().stream()
                .filter(c -> c.rank().name().equals(rank) && c.suit().name().equals(suit))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nie masz tej karty!"));

        Player targetPlayer = game.getPlayers().stream()
                .filter(p -> p.getName().equals(targetPlayerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego gracza: " + targetPlayerName));

        if (targetPlayer == winner) throw new IllegalArgumentException("Nie możesz oddać karty sobie!");

        winner.getHand().remove(cardToGive);
        targetPlayer.receiveCard(cardToGive);

        game.incrementCardsGivenCount();

        if (game.getCardsGivenCount() == 2) {
            winner.sortHand();
            game.setPhase(GamePhase.DECLARING);
        }
    }

    // --- DEKLARACJA ---
    public void declareBid(int points) {
        if (game.getPhase() != GamePhase.DECLARING) throw new IllegalStateException("To nie czas na deklarację!");
        if (game.getCurrentPlayerIndex() != game.getHighestBidderIndex()) throw new IllegalStateException("Tylko zwycięzca licytacji może deklarować!");

        if (points < game.getCurrentBid()) throw new IllegalArgumentException("Nie możesz zadeklarować mniej niż wylicytowałeś!");

        // Tu też sprawdzamy MAX (bo po dobraniu musika mógł dostać nowy meldunek!)
        Player winner = game.getPlayers().get(game.getCurrentPlayerIndex());
        int maxPossible = calculateMaxPossibleScore(winner);
        if (points > maxPossible) {
            throw new IllegalArgumentException("Masz za słabe karty na " + points + "! Twój max to: " + maxPossible);
        }

        game.setCurrentBid(points);
        game.setPhase(GamePhase.PLAYING);
    }

    // --- ROZGRYWKA ---
    public void playCard(String rank, String suit) {
        if (!game.isReadyToStart()) throw new IllegalStateException("Czekamy na graczy.");
        if (game.getPhase() == GamePhase.DECLARING) throw new IllegalStateException("Najpierw zadeklaruj punkty!");
        if (game.getPhase() == GamePhase.BIDDING) throw new IllegalStateException("Najpierw licytacja!");
        if (game.getPhase() == GamePhase.SHARING) throw new IllegalStateException("Najpierw oddaj karty!");

        int currentPlayerIndex = game.getCurrentPlayerIndex();
        Player currentPlayer = game.getPlayers().get(currentPlayerIndex);

        Card cardToPlay = currentPlayer.getHand().stream()
                .filter(c -> c.rank().name().equals(rank) && c.suit().name().equals(suit))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nie masz tej karty!"));

        validateMove(currentPlayer, cardToPlay);

        if (game.getTable().isEmpty()) checkForMeld(currentPlayer, cardToPlay);

        currentPlayer.getHand().remove(cardToPlay);
        game.getTable().add(cardToPlay);

        if (game.getTable().size() == 3) {
            finishTrick();
        } else {
            game.advanceTurn();
        }
    }

    private void validateMove(Player player, Card card) {
        if (game.getTable().isEmpty()) return;
        Card leadingCard = game.getTable().get(0);
        Suit leadingSuit = leadingCard.suit();
        Suit trump = game.getTrumpSuit();

        boolean hasLeadingSuit = player.getHand().stream().anyMatch(c -> c.suit() == leadingSuit);
        boolean hasTrump = (trump != null) && player.getHand().stream().anyMatch(c -> c.suit() == trump);

        if (hasLeadingSuit) {
            if (card.suit() != leadingSuit) throw new IllegalArgumentException("Musisz dorzucić do koloru!");
        } else if (hasTrump) {
            if (card.suit() != trump) throw new IllegalArgumentException("Musisz przebić atutem!");
        }
    }

    private void checkForMeld(Player player, Card card) {
        if (card.rank() != Rank.QUEEN && card.rank() != Rank.KING) return;
        Rank neededRank = (card.rank() == Rank.KING) ? Rank.QUEEN : Rank.KING;
        boolean hasSpouse = player.getHand().stream().anyMatch(c -> c.suit() == card.suit() && c.rank() == neededRank);

        if (hasSpouse) {
            int points = calculateMeldPoints(card.suit());
            player.addRoundPoints(points);
            game.setTrumpSuit(card.suit());
        }
    }

    private int calculateMeldPoints(Suit suit) {
        switch (suit) {
            case HEARTS: return 100; case DIAMONDS: return 80; case CLUBS: return 60; case SPADES: return 40; default: return 0;
        }
    }

    private void finishTrick() {
        List<Card> table = game.getTable();
        Suit currentTrump = game.getTrumpSuit();
        Card winningCard = table.get(0);
        int winningIndexRelative = 0;

        for (int i = 1; i < table.size(); i++) {
            Card current = table.get(i);
            if (currentTrump != null && current.suit() == currentTrump && winningCard.suit() != currentTrump) {
                winningCard = current; winningIndexRelative = i;
            } else if (currentTrump != null && current.suit() == currentTrump && winningCard.suit() == currentTrump) {
                if (current.rank().compareTo(winningCard.rank()) < 0) {
                    winningCard = current; winningIndexRelative = i;
                }
            } else if (current.suit() == winningCard.suit()) {
                if (current.rank().compareTo(winningCard.rank()) < 0) {
                    winningCard = current; winningIndexRelative = i;
                }
            }
        }
        int winnerIndex = (game.getCurrentPlayerIndex() - (2 - winningIndexRelative) + 3) % 3;
        Player winner = game.getPlayers().get(winnerIndex);

        int points = table.stream().mapToInt(Card::getPoints).sum();
        winner.addRoundPoints(points);

        table.clear();
        game.setCurrentPlayerIndex(winnerIndex);

        boolean roundOver = game.getPlayers().stream().allMatch(p -> p.getHand().isEmpty());
        if (roundOver) {
            endRound();
        }
    }

    private void endRound() {
        Player bidder = game.getPlayers().get(game.getHighestBidderIndex());
        int bidAmount = game.getCurrentBid();

        if (bidder.getRoundScore() >= bidAmount) {
            bidder.setScore(bidder.getScore() + bidAmount);
        } else {
            bidder.setScore(bidder.getScore() - bidAmount);
        }
        bidder.resetRoundScore();

        for (Player p : game.getPlayers()) {
            if (p != bidder) {
                int rounded = roundPoints(p.getRoundScore());
                p.setScore(p.getScore() + rounded);
                p.resetRoundScore();
            }
        }

        // Sprawdzamy wygraną (1000 pkt)
        for (Player p : game.getPlayers()) {
            if (p.getScore() >= 1000) {
                game.setPhase(GamePhase.GAME_OVER);
                return;
            }
        }
        game.startNewRound();
    }

    private int roundPoints(int points) {
        if (points == 0) return 0;
        int remainder = points % 10;
        return remainder >= 5 ? points + (10 - remainder) : points - remainder;
    }
}
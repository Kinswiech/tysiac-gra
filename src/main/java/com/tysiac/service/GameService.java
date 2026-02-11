package com.tysiac.service;

import com.tysiac.model.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GameService {
    //obiekt do stanu gry
    private Game game = new Game();

    //twardy reset gry
    public void resetGame() {
        this.game = new Game();
    }

    public Game getGame() {
        return game;
    }

    //logika dołączania do gry
    public void addPlayer(String playerName) {
        //sprawdza, czy gra już trwa
        if (game.isReadyToStart()) {
            boolean exists = game.getPlayers().stream().anyMatch(p -> p.getName().equals(playerName));
            if (!exists) throw new IllegalStateException("Gra jest w toku. Stół pełny.");
            return;
        }

        //dodanie gracza
        game.addPlayer(playerName);

        //start gdy jest trzech graczy
        if (game.isReadyToStart()) {
            game.startNewRound();
            System.out.println("3 graczy zebranych. START!");
        }
    }

    public void joinGame(String playerName) {
        addPlayer(playerName);
    }

    //logika licytacji
    public void bid(int amount) {
        if (game.getPhase() != GamePhase.BIDDING) throw new IllegalStateException("To nie licytacja!");

        //gracz pasuje
        if (amount == 0) {
            game.incrementPassCount();
            //jeśli dwóch graczy spasuje, trzeci musi iść
            if (game.getPassCount() == 2) {
                int winnerIndex = (game.getCurrentPlayerIndex() + 1) % 3;
                game.setHighestBidderIndex(winnerIndex);

                //faza rozdawania
                game.setPhase(GamePhase.SHARING);
                game.setCurrentPlayerIndex(winnerIndex);

                //zwycięzca bierze musik
                Player winner = game.getPlayers().get(winnerIndex);
                winner.getHand().addAll(game.getMusik());
                winner.sortHand();
                game.getMusik().clear();
            } else {
                game.advanceTurn();
            }
            //gracz podbija stawkę
        } else {
            if (amount <= game.getCurrentBid()) {
                throw new IllegalArgumentException("Musisz dać więcej niż " + game.getCurrentBid());
            }

            //sprawdza, czy gracz może ugrać tyle ile chce zadeklarować
            Player currentPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());
            int maxPossible = calculateMaxPossibleScore(currentPlayer);

            if (amount > maxPossible) {
                throw new IllegalArgumentException("Nie możesz licytować " + amount + "! Twój max (120 + meldunki w ręku) to: " + maxPossible);
            }

            game.setCurrentBid(amount); //zatwierdzenie nowej stawki
            game.setHighestBidderIndex(game.getCurrentPlayerIndex());
            game.resetPassCount();
            game.advanceTurn();
        }
    }

    //helpery
    private int calculateMaxPossibleScore(Player player) {
        int maxScore = 120;
        if (hasMarriage(player, Suit.HEARTS)) maxScore += 100;
        if (hasMarriage(player, Suit.DIAMONDS)) maxScore += 80;
        if (hasMarriage(player, Suit.CLUBS)) maxScore += 60;
        if (hasMarriage(player, Suit.SPADES)) maxScore += 40;
        return maxScore;
    }

    //sprawdza czy gracz ma meldunek
    private boolean hasMarriage(Player player, Suit suit) {
        boolean hasKing = player.getHand().stream().anyMatch(c -> c.suit() == suit && c.rank() == Rank.KING);
        boolean hasQueen = player.getHand().stream().anyMatch(c -> c.suit() == suit && c.rank() == Rank.QUEEN);
        return hasKing && hasQueen;
    }

    //oddawanie kart
    public void shareCard(String rank, String suit, String targetPlayerName) {
        if (game.getPhase() != GamePhase.SHARING) throw new IllegalStateException("To nie czas na oddawanie!");

        Player winner = game.getPlayers().get(game.getHighestBidderIndex());

        Card cardToGive = winner.getHand().stream()
                .filter(c -> c.rank().name().equals(rank) && c.suit().name().equals(suit))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nie masz tej karty!"));

        //znajdź gracza któremu oddajemy kartę
        Player targetPlayer = game.getPlayers().stream()
                .filter(p -> p.getName().equals(targetPlayerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nie ma takiego gracza: " + targetPlayerName));

        if (targetPlayer == winner) throw new IllegalArgumentException("Nie możesz oddać karty sobie!");

        //przekazanie karty
        winner.getHand().remove(cardToGive);
        targetPlayer.receiveCard(cardToGive);

        game.incrementCardsGivenCount();

        //jeśli karty oddane, to deklaracja
        if (game.getCardsGivenCount() == 2) {
            winner.sortHand();
            game.setPhase(GamePhase.DECLARING);
        }
    }


    //deklaracja ostatecznej stawki
    public void declareBid(int points) {
        if (game.getPhase() != GamePhase.DECLARING) throw new IllegalStateException("To nie czas na deklarację!");
        if (game.getCurrentPlayerIndex() != game.getHighestBidderIndex()) throw new IllegalStateException("Tylko zwycięzca licytacji może deklarować!");

        if (points < game.getCurrentBid()) throw new IllegalArgumentException("Nie możesz zadeklarować mniej niż wylicytowałeś!");

        //znowu sprawdzamy, czy gracz ugra
        Player winner = game.getPlayers().get(game.getCurrentPlayerIndex());
        int maxPossible = calculateMaxPossibleScore(winner);
        if (points > maxPossible) {
            throw new IllegalArgumentException("Masz za słabe karty na " + points + "! Twój max to: " + maxPossible);
        }

        game.setCurrentBid(points);
        game.setPhase(GamePhase.PLAYING);
    }

    //rozgrywka
    public void playCard(String rank, String suit) {
        //podstawowe walidacje fazy gry i kolejki
        if (!game.isReadyToStart()) throw new IllegalStateException("Czekamy na graczy.");
        if (game.getPhase() == GamePhase.DECLARING) throw new IllegalStateException("Najpierw zadeklaruj punkty!");
        if (game.getPhase() == GamePhase.BIDDING) throw new IllegalStateException("Najpierw licytacja!");
        if (game.getPhase() == GamePhase.SHARING) throw new IllegalStateException("Najpierw oddaj karty!");

        int currentPlayerIndex = game.getCurrentPlayerIndex();
        Player currentPlayer = game.getPlayers().get(currentPlayerIndex);

        //znajdź kartę na ręku gracza
        Card cardToPlay = currentPlayer.getHand().stream()
                .filter(c -> c.rank().name().equals(rank) && c.suit().name().equals(suit))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nie masz tej karty!"));

        //sprawdź czy musi przebić/rzucić do koloru
        validateMove(currentPlayer, cardToPlay);

        //sprawdź czy rzuca meldunek
        if (game.getTable().isEmpty()) checkForMeld(currentPlayer, cardToPlay);

        //wykonuje ruch
        currentPlayer.getHand().remove(cardToPlay);
        game.getTable().add(cardToPlay);

        //jeśli trzy karty na stole, koniec kolejki
        if (game.getTable().size() == 3) {
            finishTrick();
        } else {
            game.advanceTurn();
        }
    }

    //walidacja ruchu
    private void validateMove(Player player, Card card) {
        if (game.getTable().isEmpty()) return; //pierwsza rzucona karta jest dowolna
        Card leadingCard = game.getTable().get(0);
        Suit leadingSuit = leadingCard.suit();
        Suit trump = game.getTrumpSuit(); //aktualny atut

        boolean hasLeadingSuit = player.getHand().stream().anyMatch(c -> c.suit() == leadingSuit);
        boolean hasTrump = (trump != null) && player.getHand().stream().anyMatch(c -> c.suit() == trump);

        //musisz dorzucić do koloru
        if (hasLeadingSuit) {
            if (card.suit() != leadingSuit) throw new IllegalArgumentException("Musisz dorzucić do koloru!");
            //jeśli nie kolor a masz atut, to rzucasz atut
        } else if (hasTrump) {
            if (card.suit() != trump) throw new IllegalArgumentException("Musisz przebić atutem!");
        }
        //jeśli nie jedno i drugie rzucasz cokolwiek
    }

    //meldowanie
    private void checkForMeld(Player player, Card card) {
        if (card.rank() != Rank.QUEEN && card.rank() != Rank.KING) return;
        //sprawdza czy gracz ma meldunek
        Rank neededRank = (card.rank() == Rank.KING) ? Rank.QUEEN : Rank.KING;
        boolean hasSpouse = player.getHand().stream().anyMatch(c -> c.suit() == card.suit() && c.rank() == neededRank);

        //dodanie punktów i ustawienie atutu
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

    //koniec rundy
    private void finishTrick() {
        List<Card> table = game.getTable();
        Suit currentTrump = game.getTrumpSuit();
        Card winningCard = table.get(0); //domyślnie wygrywa najwyższa karta
        int winningIndexRelative = 0;

        //porównanie pozostałych kart
        for (int i = 1; i < table.size(); i++) {
            Card current = table.get(i);
            //wygrywa karta atut
            if (currentTrump != null && current.suit() == currentTrump && winningCard.suit() != currentTrump) {
                winningCard = current; winningIndexRelative = i;
                //obie atut, decyduje waga
            } else if (currentTrump != null && current.suit() == currentTrump && winningCard.suit() == currentTrump) {
                if (current.rank().compareTo(winningCard.rank()) < 0) {
                    winningCard = current; winningIndexRelative = i;
                }
                //kolor ten sam co wygrywająca, decyduje starszeństwo
            } else if (current.suit() == winningCard.suit()) {
                if (current.rank().compareTo(winningCard.rank()) < 0) {
                    winningCard = current; winningIndexRelative = i;
                }
            }
        }
        //obliczamy, który to był gracz
        int winnerIndex = (game.getCurrentPlayerIndex() - (2 - winningIndexRelative) + 3) % 3;
        Player winner = game.getPlayers().get(winnerIndex);

        //zliczanie punkty z kart na stole
        int points = table.stream().mapToInt(Card::getPoints).sum();
        winner.addRoundPoints(points);

        //czyszczenie stołu
        table.clear();
        game.setCurrentPlayerIndex(winnerIndex);

        //sprawdzanie czy koniec rundy
        boolean roundOver = game.getPlayers().stream().allMatch(p -> p.getHand().isEmpty());
        if (roundOver) {
            endRound();
        }
    }

    //liczenie punktów po rundzie
    private void endRound() {
        Player bidder = game.getPlayers().get(game.getHighestBidderIndex());
        int bidAmount = game.getCurrentBid();

        //rozliczanie licytująceog, jeśli ugrał dodajemy, jak nie odejmujemy
        if (bidder.getRoundScore() >= bidAmount) {
            bidder.setScore(bidder.getScore() + bidAmount);
        } else {
            bidder.setScore(bidder.getScore() - bidAmount);
        }
        bidder.resetRoundScore();

        //rozliczenie pozostałych z zaokrągleniem
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
        //jeśli nikt nie wygrał to nowa runda
        game.startNewRound();
    }

    //zaokrąglenie punktów
    private int roundPoints(int points) {
        if (points == 0) return 0;
        int remainder = points % 10;
        return remainder >= 5 ? points + (10 - remainder) : points - remainder;
    }
}
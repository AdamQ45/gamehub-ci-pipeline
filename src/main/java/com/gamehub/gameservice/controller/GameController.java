package com.gamehub.gameservice.controller;

// GameController - REST API for game management

import com.gamehub.gameservice.client.PlayerClient;
import com.gamehub.gameservice.config.GameHubConfig;
import com.gamehub.gameservice.dto.PlayerDto;
import com.gamehub.gameservice.dto.PurchaseRequest;
import com.gamehub.gameservice.dto.PurchaseResponse;
import com.gamehub.gameservice.entity.Game;
import com.gamehub.gameservice.entity.Purchase;
import com.gamehub.gameservice.repository.GameRepository;
import com.gamehub.gameservice.repository.PurchaseRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameRepository gameRepository;
    private final PurchaseRepository purchaseRepository;
    private final PlayerClient playerClient;
    private final Environment environment;
    private final GameHubConfig gameHubConfig;

    public GameController(GameRepository gameRepository, PurchaseRepository purchaseRepository,
                          PlayerClient playerClient, Environment environment, GameHubConfig gameHubConfig) {
        this.gameRepository = gameRepository;
        this.purchaseRepository = purchaseRepository;
        this.playerClient = playerClient;
        this.environment = environment;
        this.gameHubConfig = gameHubConfig;
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        return Map.of(
                "environment", gameHubConfig.getEnvironment(),
                "salesEnabled", gameHubConfig.isSalesEnabled(),
                "port", environment.getProperty("local.server.port", "unknown"));
    }

    @GetMapping
    public List<Game> getAllGames() {
        log.info("getAllGames served by instance on port: {}", environment.getProperty("local.server.port"));
        return gameRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Game> getGame(@PathVariable Long id) {
        return gameRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Game createGame(@RequestBody Game game) {
        return gameRepository.save(game);
    }

    @PostMapping("/purchase")
    @CircuitBreaker(name = "playerService", fallbackMethod = "purchaseFallback")
    @Retry(name = "playerService")
    public ResponseEntity<?> purchaseGame(@RequestBody PurchaseRequest request) {
        log.info("Purchase request: playerId={}, gameId={}", request.getPlayerId(), request.getGameId());

        Game game = gameRepository.findById(request.getGameId()).orElse(null);
        if (game == null) {
            return ResponseEntity.badRequest().body("Game not found");
        }

        PlayerDto player = playerClient.getPlayer(request.getPlayerId());
        if (player == null) {
            return ResponseEntity.badRequest().body("Player not found");
        }

        if (player.getBalance() < game.getPrice()) {
            return ResponseEntity.badRequest().body("Insufficient funds. Balance: " +
                    player.getBalance() + ", Price: " + game.getPrice());
        }

        PlayerDto updated = playerClient.debitPlayer(request.getPlayerId(),
                Map.of("amount", game.getPrice()));

        Purchase purchase = new Purchase(game.getId(), player.getId(), game.getPrice());
        purchaseRepository.save(purchase);

        log.info("Purchase successful: {} bought {} for {}", player.getUsername(), game.getTitle(), game.getPrice());

        return ResponseEntity.ok(new PurchaseResponse(
                "SUCCESS", game.getTitle(), player.getUsername(),
                game.getPrice(), updated.getBalance()));
    }

    public ResponseEntity<?> purchaseFallback(PurchaseRequest request, Throwable t) {
        log.error("Player service unavailable, purchase failed: {}", t.getMessage());
        return ResponseEntity.ok(new PurchaseResponse(
                "FAILED - Player service unavailable. Please try again later.",
                null, null, null, null));
    }

    @GetMapping("/player/{playerId}/library")
    @CircuitBreaker(name = "playerService", fallbackMethod = "libraryFallback")
    public ResponseEntity<?> getPlayerLibrary(@PathVariable Long playerId) {
        PlayerDto player = playerClient.getPlayer(playerId);
        if (player == null) {
            return ResponseEntity.notFound().build();
        }

        List<Purchase> purchases = purchaseRepository.findByPlayerId(playerId);
        List<Long> gameIds = purchases.stream().map(Purchase::getGameId).toList();
        List<Game> games = gameRepository.findAllById(gameIds);

        return ResponseEntity.ok(Map.of(
                "player", player.getUsername(),
                "balance", player.getBalance(),
                "games", games));
    }

    public ResponseEntity<?> libraryFallback(Long playerId, Throwable t) {
        log.error("Player service unavailable for library lookup: {}", t.getMessage());
        List<Purchase> purchases = purchaseRepository.findByPlayerId(playerId);
        List<Long> gameIds = purchases.stream().map(Purchase::getGameId).toList();
        List<Game> games = gameRepository.findAllById(gameIds);

        return ResponseEntity.ok(Map.of(
                "player", "unavailable",
                "games", games,
                "note", "Player details temporarily unavailable"));
    }
}

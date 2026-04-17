package com.gamehub.gameservice.dto;

public class PurchaseRequest {
    private Long playerId;
    private Long gameId;

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }
}

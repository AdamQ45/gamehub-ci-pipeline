package com.gamehub.gameservice.dto;

import java.time.LocalDateTime;

public class PurchaseResponse {
    private String status;
    private String gameTitle;
    private String playerUsername;
    private Double amountCharged;
    private Double remainingBalance;
    private LocalDateTime purchaseDate;

    public PurchaseResponse(String status, String gameTitle, String playerUsername,
                            Double amountCharged, Double remainingBalance) {
        this.status = status;
        this.gameTitle = gameTitle;
        this.playerUsername = playerUsername;
        this.amountCharged = amountCharged;
        this.remainingBalance = remainingBalance;
        this.purchaseDate = LocalDateTime.now();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }
    public String getPlayerUsername() { return playerUsername; }
    public void setPlayerUsername(String playerUsername) { this.playerUsername = playerUsername; }
    public Double getAmountCharged() { return amountCharged; }
    public void setAmountCharged(Double amountCharged) { this.amountCharged = amountCharged; }
    public Double getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(Double remainingBalance) { this.remainingBalance = remainingBalance; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
}

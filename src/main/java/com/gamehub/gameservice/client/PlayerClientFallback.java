package com.gamehub.gameservice.client;

import com.gamehub.gameservice.dto.PlayerDto;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PlayerClientFallback implements PlayerClient {

    @Override
    public PlayerDto getPlayer(Long id) {
        return null;
    }

    @Override
    public Double getBalance(Long id) {
        return null;
    }

    @Override
    public PlayerDto debitPlayer(Long id, Map<String, Double> request) {
        return null;
    }
}

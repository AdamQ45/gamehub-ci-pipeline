package com.gamehub.gameservice.client;

import com.gamehub.gameservice.dto.PlayerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "PLAYER-SERVICE")
public interface PlayerClient {

    @GetMapping("/api/players/{id}")
    PlayerDto getPlayer(@PathVariable("id") Long id);

    @GetMapping("/api/players/{id}/balance")
    Double getBalance(@PathVariable("id") Long id);

    @PostMapping("/api/players/{id}/debit")
    PlayerDto debitPlayer(@PathVariable("id") Long id, @RequestBody Map<String, Double> request);
}

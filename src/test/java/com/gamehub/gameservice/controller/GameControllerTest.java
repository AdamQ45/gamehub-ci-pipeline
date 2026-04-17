package com.gamehub.gameservice.controller;

import com.gamehub.gameservice.client.PlayerClient;
import com.gamehub.gameservice.config.GameHubConfig;
import com.gamehub.gameservice.entity.Game;
import com.gamehub.gameservice.repository.GameRepository;
import com.gamehub.gameservice.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
@ActiveProfiles("test")
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private PurchaseRepository purchaseRepository;

    @MockBean
    private PlayerClient playerClient;

    @MockBean
    private GameHubConfig gameHubConfig;

    @Test
    void getAllGames_returnsGameList() throws Exception {
        Game game = new Game("Elden Ring", "RPG", "PC", 59.99);
        game.setId(1L);
        when(gameRepository.findAll()).thenReturn(List.of(game));

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Elden Ring"))
                .andExpect(jsonPath("$[0].genre").value("RPG"));
    }

    @Test
    void getGame_found_returnsGame() throws Exception {
        Game game = new Game("Hades", "Roguelike", "PC", 24.99);
        game.setId(1L);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        mockMvc.perform(get("/api/games/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Hades"));
    }

    @Test
    void getGame_notFound_returns404() throws Exception {
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/games/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createGame_returnsCreatedGame() throws Exception {
        Game game = new Game("Hollow Knight", "Metroidvania", "PC", 14.99);
        game.setId(1L);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hollow Knight\",\"genre\":\"Metroidvania\",\"platform\":\"PC\",\"price\":14.99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Hollow Knight"))
                .andExpect(jsonPath("$.price").value(14.99));
    }
}

package com.gamehub.gameservice.controller;

import com.gamehub.gameservice.entity.Game;
import com.gamehub.gameservice.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GameControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GameRepository gameRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/games";
        gameRepository.deleteAll();
    }

    @Test
    void createAndRetrieveGame() {
        Game game = new Game("Test Game", "Action", "PC", 29.99);

        ResponseEntity<Game> createResponse = restTemplate.postForEntity(baseUrl, game, Game.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getTitle()).isEqualTo("Test Game");

        Long id = createResponse.getBody().getId();
        ResponseEntity<Game> getResponse = restTemplate.getForEntity(baseUrl + "/" + id, Game.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getPrice()).isEqualTo(29.99);
    }

    @Test
    void getAllGames_returnsSeededData() {
        gameRepository.save(new Game("Game A", "RPG", "PC", 19.99));
        gameRepository.save(new Game("Game B", "FPS", "PS5", 59.99));

        ResponseEntity<Game[]> response = restTemplate.getForEntity(baseUrl, Game[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getGame_notFound_returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

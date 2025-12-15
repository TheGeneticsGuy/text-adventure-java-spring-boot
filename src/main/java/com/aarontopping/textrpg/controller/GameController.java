package com.aarontopping.textrpg.controller;

import com.aarontopping.textrpg.model.GameSession;
import com.aarontopping.textrpg.service.GameService;
import com.aarontopping.textrpg.service.story.Choice;
import com.aarontopping.textrpg.service.story.Scene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

record GameStateResponse(
        Long sessionId,
        String sceneId,
        String description,
        String imageUrl,
        List<Choice> choices,
        boolean gameOver,
        String outcomeMessage,
        Long playerId,
        String playerName,
        int rigging,
        int logic,
        int nerve
) {}

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    static class StartGameRequest {
        public String playerName;
    }

    @PostMapping("/start")
    public ResponseEntity<GameStateResponse> startGame(@RequestBody StartGameRequest request) {
        if (request.playerName == null || request.playerName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player name cannot be empty");
        }
        try {
            GameSession session = gameService.startGame(request.playerName);
            Scene currentScene = gameService.getSceneDetails(session.getId());
            return ResponseEntity.ok(createGameStateResponse(session, currentScene));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error starting game: " + e.getMessage(), e);
        }
    }

    static class MakeChoiceRequest {
        public String choiceId;
    }

    @PostMapping("/{sessionId}/action")
    public ResponseEntity<GameStateResponse> makeChoice(
            @PathVariable Long sessionId,
            @RequestBody MakeChoiceRequest request) {
        try {
            GameSession session = gameService.processChoice(sessionId, request.choiceId);
            Scene currentScene = gameService.getSceneDetails(sessionId);
            return ResponseEntity.ok(createGameStateResponse(session, currentScene));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing choice: " + e.getMessage(), e);
        }
    }

    private GameStateResponse createGameStateResponse(GameSession session, Scene scene) {
        return new GameStateResponse(
                session.getId(),
                scene.getId(),
                scene.getDescription(),
                scene.getImageUrl(),
                scene.getChoices(),
                session.isGameOver(),
                session.getGameOutcomeMessage(),
                session.getPlayer().getId(),
                session.getPlayer().getName(),
                session.getPlayer().getRigging(),
                session.getPlayer().getLogic(),
                session.getPlayer().getNerve()
        );
    }
}
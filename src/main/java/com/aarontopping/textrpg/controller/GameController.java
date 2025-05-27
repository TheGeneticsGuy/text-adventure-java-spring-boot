package com.aarontopping.textrpg.controller;

import com.aarontopping.textrpg.model.GameSession;
import com.aarontopping.textrpg.service.GameService;
import com.aarontopping.textrpg.service.story.Scene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // This handles all error responses properly

import java.util.List;
import com.aarontopping.textrpg.service.story.Choice;

// GameStateResponse DTO (can be a record or a class)
record GameStateResponse(
        Long sessionId,
        String sceneId,
        String description,
        List<Choice> choices, // Use your Choice DTO
        boolean gameOver,
        String outcomeMessage,
        Long playerId,
        String playerName,
        String playerClass, // New: Add player class info
        List<String> knownSkills // New: Add player skills
) {
}

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
            Scene currentScene = gameService.getSceneDetails(session.getId()); // Pass sessionId
            return ResponseEntity.ok(createGameStateResponse(session, currentScene));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error starting game: " + e.getMessage(), e);
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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error processing choice: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{sessionId}/state")
    public ResponseEntity<GameStateResponse> getGameState(@PathVariable Long sessionId) {
        try {
            return gameService.getGameSession(sessionId)
                    .map(session -> {
                        Scene currentScene = gameService.getSceneDetails(sessionId);
                        return ResponseEntity.ok(createGameStateResponse(session, currentScene));
                    })
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game session not found"));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    // Method to create the response
    private GameStateResponse createGameStateResponse(GameSession session, Scene scene) {
        String playerClassName = session.getPlayer().getCharacterClass() != null
                ? session.getPlayer().getCharacterClass().getClassName()
                : "Undetermined";
        List<String> skillNames = session.getPlayer().getKnownSkills().stream()
                .map(com.aarontopping.textrpg.model.Skill::getName) // Using the Skill Model here
                .collect(java.util.stream.Collectors.toList());

        return new GameStateResponse(
                session.getId(),
                scene.getId(),
                scene.getDescription(),
                scene.getChoices(),
                session.isGameOver(),
                session.getGameOutcomeMessage(),
                session.getPlayer().getId(),
                session.getPlayer().getName(),
                playerClassName,
                skillNames);
    }
}
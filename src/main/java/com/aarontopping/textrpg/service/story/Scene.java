package com.aarontopping.textrpg.service.story;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scene {
    private String id;
    private String description;
    private List<Choice> choices = new ArrayList<>();
    private boolean isEndingScene = false; // Marks if this scene ends the game
    private boolean isBattleScene = false; // For future expansion
    // For example, if isBattleScene is true, I could add an ememyId, but that is
    // beyond the complexity
    // of this Project for CSE310. I this it will be fun, so I will probably expand
    // for my personal portfolio.

    public Scene(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public Scene(String id, String description, boolean isEndingScene) {
        this.id = id;
        this.description = description;
        this.isEndingScene = isEndingScene;
    }

    public Scene(String id, String description, boolean isEndingScene, boolean isBattleScene) {
        this.id = id;
        this.description = description;
        this.isEndingScene = isEndingScene;
        this.isBattleScene = isBattleScene;
    }

    public void addChoice(Choice choice) {
        if (this.choices == null) { // Just adding some redundancy but shouldn't happen
            this.choices = new ArrayList<>();
        }
        this.choices.add(choice);
    }

    public boolean isEndingScene() {
        return isEndingScene;
    }

    public void setEndingScene(boolean endingScene) {
        isEndingScene = endingScene;
    }

    public boolean isBattleScene() {
        return isBattleScene;
    }

    public void setBattleScene(boolean battleScene) {
        isBattleScene = battleScene;
    }
}
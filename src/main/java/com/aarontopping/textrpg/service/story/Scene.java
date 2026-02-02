package com.aarontopping.textrpg.service.story;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scene {
    private String id;
    private String description;
    private String imageUrl; // Can be null
    private List<Choice> choices = new ArrayList<>();
    private boolean isEndingScene = false;

    // Basic Scene (ID + Description)
    public Scene(String id, String description) {
        this.id = id;
        this.description = description;
    }

    // Ending Scene (ID + Description + IsEnding)
    // This fixes the error you are seeing
    public Scene(String id, String description, boolean isEndingScene) {
        this.id = id;
        this.description = description;
        this.isEndingScene = isEndingScene;
    }

    // Scene with Image (ID + Description + ImageUrl)
    public Scene(String id, String description, String imageUrl) {
        this.id = id;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Ending Scene with Image (ID + Desc + Image + IsEnding)
    public Scene(String id, String description, String imageUrl, boolean isEndingScene) {
        this.id = id;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isEndingScene = isEndingScene;
    }

    public void addChoice(Choice choice) {
        this.choices.add(choice);
    }
}
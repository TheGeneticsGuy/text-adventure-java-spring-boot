package com.aarontopping.textrpg.service.story;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Choice {
    private String id;
    private String text;
    private String nextSceneId;
    private String skillName;   // Optional: null for story choices
    private boolean requiresPlayerAction = false;

    // This allows us to create simple story choices
    public Choice(String id, String text, String nextSceneId) {
        this.id = id;
        this.text = text;
        this.nextSceneId = nextSceneId;
        this.skillName = null;
        this.requiresPlayerAction = false;
    }
}
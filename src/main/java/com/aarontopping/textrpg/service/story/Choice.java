package com.aarontopping.textrpg.service.story;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Choice {
    private String id; // "1", "ATTACK_WITH_POWER_SLASH"
    private String text; // "Use Power Slash"
    private String nextSceneId; // Scene after this choice
    private String skillName; // Name of skill choice uses
    private boolean requiresPlayerAction = false; // True if this choice is a game mechanic like "attack"
}
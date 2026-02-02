package com.aarontopping.textrpg.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // The "Investigator" Stats
    private int rigging = 0;   // Engineering
    private int acoustics = 0; // Perception/Sound
    private int logic = 0;     // Decryption
    private int nerve = 100;   // Health/Sanity

    // Story Flags
    private boolean coilsAligned = false;
    private boolean coolingSystemFixed = false;
    private boolean journalRead = false;
    private boolean hasKey = false; // The artifact

    public Player(String name) {
        this.name = name;
        // Default stats without choosing yet.
        this.rigging = 1;
        this.acoustics = 1;
        this.logic = 1;
        this.nerve = 100;
    }
}
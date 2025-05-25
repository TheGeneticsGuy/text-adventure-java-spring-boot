package com.aarontopping.textrpg.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int health;

    @ManyToOne(cascade = { CascadeType.MERGE }) // Player has only 1 character class
    @JoinColumn(name = "character_class_id")
    private CharacterClass characterClass;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true) // Player has only 1 equipped weapon
    @JoinColumn(name = "equipped_weapon_id")
    private Weapon equippedWeapon;

    // Player default skills - I won't add to this, but adding so I can scale in
    // future maybe
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
    @JoinTable(name = "player_known_skills", joinColumns = @JoinColumn(name = "player_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> knownSkills = new ArrayList<>();

    public Player(String name) {
        // Default Player Values
        this.name = name;
        this.health = 100;
    }

    // I really want to expand this in the future if they reach player milestones
    public void addSkill(Skill skill) {
        if (this.knownSkills == null) {
            this.knownSkills = new ArrayList<>();
        }
        if (!this.knownSkills.contains(skill)) {
            this.knownSkills.add(skill);
        }
    }

    public void addSkills(List<Skill> skills) {
        if (skills == null)
            return;
        skills.forEach(this::addSkill);
    }
}
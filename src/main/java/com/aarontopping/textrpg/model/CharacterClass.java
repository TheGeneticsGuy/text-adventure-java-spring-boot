package com.aarontopping.textrpg.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // All classes in one table
@DiscriminatorColumn(name = "class_type", discriminatorType = DiscriminatorType.STRING) // Columns to separate
@Data
@NoArgsConstructor
public abstract class CharacterClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String className;
    private String classDescription;

    // Each class can have a list of skills it grants by default
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
    @JoinTable(name = "class_default_skills", joinColumns = @JoinColumn(name = "character_class_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> defaultSkills = new ArrayList<>();

    public CharacterClass(String className, String classDescription) {
        this.className = className;
        this.classDescription = classDescription;
    }

    public void addDefaultSkill(Skill skill) {
        if (this.defaultSkills == null) {
            this.defaultSkills = new ArrayList<>();
        }
        if (!this.defaultSkills.contains(skill)) {
            this.defaultSkills.add(skill);
        }
    }

    public abstract String getPrimaryAttackSkillName(); // For bow, it will be "Ranged Arrow" and for Sword "Power

    public abstract String getPreferredWeaponType(); // I will only use 2 for this program, sword and bow
}
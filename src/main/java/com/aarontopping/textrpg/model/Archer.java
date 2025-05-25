package com.aarontopping.textrpg.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("ARCHER")
@Data
@EqualsAndHashCode(callSuper = true)
public class Archer extends CharacterClass {

    public Archer() {
        super("Archer", "A skilled marksman, deadly from a distance.");
    }

    @Override
    public String getPrimaryAttackSkillName() {
        return "Ranged Arrow"; // Archer's special skill
    }

    @Override
    public String getPreferredWeaponType() {
        return "BOW";
    }
}
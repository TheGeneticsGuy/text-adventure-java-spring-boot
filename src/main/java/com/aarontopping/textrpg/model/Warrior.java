package com.aarontopping.textrpg.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("WARRIOR") // Value for the discriminator column
@Data
@EqualsAndHashCode(callSuper = true)
public class Warrior extends CharacterClass {

    public Warrior() {
        super("Warrior", "A formidable fighter excelling in close-quarters combat.");
    }

    @Override
    public String getPrimaryAttackSkillName() {
        return "Power Slash"; // Warrior's special skill
    }

    @Override
    public String getPreferredWeaponType() {
        return "SWORD";
    }
}
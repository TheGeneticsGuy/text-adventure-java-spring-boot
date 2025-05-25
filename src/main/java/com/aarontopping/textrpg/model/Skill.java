package com.aarontopping.textrpg.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "name")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // "Ranged Shot", "Punch" , "Slashs" and so on
    private String description;
    private int baseDamage; // For calculating damage of a weapon

    public Skill(String name, String description, int baseDamage) {
        this.name = name;
        this.description = description;
        this.baseDamage = baseDamage;
    }
}
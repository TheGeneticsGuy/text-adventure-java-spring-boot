package com.aarontopping.textrpg.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeaponTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private int attackBonus;

    @Version
    private Long version;

    public WeaponTemplate(String name, String type, int attackBonus) {
        this.name = name;
        this.type = type;
        this.attackBonus = attackBonus;
    }
}
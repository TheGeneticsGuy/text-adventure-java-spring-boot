package com.aarontopping.textrpg.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column; // Needed to add  this as without it my H2 JPA Database maxes stored strings to 255 chars
                                   // This import  allows up to VARCHAR(1000) or VARCHAR(2048), Column allows to customize
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A GameSession is tied to one Player
    // Let's use MERGE and PERSIST is what is used when created a player IN SESSION
    @OneToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private Player player;

    private String currentSceneId;
    private boolean gameOver;

    @Column(length = 1000)
    private String gameOutcomeMessage;

    public GameSession(Player player, String initialSceneId) {
        this.player = player;
        this.currentSceneId = initialSceneId;
        this.gameOver = false;
    }
}
package com.aarontopping.textrpg.service;

import com.aarontopping.textrpg.model.*;
import com.aarontopping.textrpg.repository.*;
import com.aarontopping.textrpg.service.story.Choice;
import com.aarontopping.textrpg.service.story.Scene;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GameService {

    private final PlayerRepository playerRepository;
    private final GameSessionRepository gameSessionRepository;
    private final Map<String, Scene> storyScenes = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    // --- SCENE CONSTANTS ---
    public static final String SCENE_LOGIN_TERMINAL = "LOGIN_TERMINAL";
    public static final String SCENE_LAB_HUB = "LAB_HUB";
    public static final String SCENE_INSPECT_COILS = "INSPECT_COILS";
    public static final String SCENE_COILS_ALIGNED = "COILS_ALIGNED";
    public static final String SCENE_CHECK_PC = "CHECK_PC";
    public static final String SCENE_READ_JOURNAL = "READ_JOURNAL";
    public static final String SCENE_FLASHBACK = "FLASHBACK"; // The video/image trigger
    public static final String SCENE_ENTITY_APPEARS = "ENTITY_APPEARS";
    public static final String SCENE_ENTITY_DEATH = "ENTITY_DEATH"; // Game Over
    public static final String SCENE_ACTIVATE_PORTAL = "ACTIVATE_PORTAL";
    public static final String SCENE_JUMP_PORTAL = "JUMP_PORTAL"; // Chapter 1 End

    @Autowired
    public GameService(PlayerRepository playerRepository, GameSessionRepository gameSessionRepository) {
        this.playerRepository = playerRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    @PostConstruct
    public void initializeStory() {
        storyScenes.clear();

        // 1. The Hook: The Terminal Boot
        Scene login = new Scene(SCENE_LOGIN_TERMINAL,
            "OS v9.4.2 BOOT SEQUENCE INITIATED...\n\n" +
            "Loading Kernel... OK.\n" +
            "Mounting Volume: 'Bell-Resonance'... OK.\n" +
            "User Detected via Biometrics.\n\n" +
            "Welcome to Lab 4. The hum of the cooling fans is the only sound. The air smells of ozone and stale coffee. " +
            "The Quantum-Cymatic Array sits silent in the center of the room.",
            "https://placehold.co/600x400/000000/00FF00?text=TERMINAL+BOOT" // Placeholder Image
        );
        login.addChoice(new Choice("START_WORK", "Initialize Lab Protocol", SCENE_LAB_HUB));
        storyScenes.put(SCENE_LOGIN_TERMINAL, login);

        // 2. The Hub
        Scene labHub = new Scene(SCENE_LAB_HUB,
            "You stand before the Array. Copper coils loom over a central platform. " +
            "To your left is the Control PC. On a cluttered desk lies a dust-covered Journal. " +
            "The protocol requires the coils to be aligned and the system booted before engagement.");
        // Choices generated dynamically based on flags
        storyScenes.put(SCENE_LAB_HUB, labHub);

        // 3. The Puzzles
        Scene inspectCoils = new Scene(SCENE_INSPECT_COILS,
            "You examine Emitter A. It's misaligned by 15 degrees. The geometry must be perfect to create the standing wave. " +
            "You grab the heavy wrench.");
        inspectCoils.addChoice(new Choice("ALIGN_COILS", "Rotate Coil A (Requires Rigging)", SCENE_COILS_ALIGNED));
        inspectCoils.addChoice(new Choice("BACK", "Step away", SCENE_LAB_HUB));
        storyScenes.put(SCENE_INSPECT_COILS, inspectCoils);

        Scene coilsAligned = new Scene(SCENE_COILS_ALIGNED,
            "With a groan of metal, the coil locks into place. You feel a subtle vibration in your teeth. " +
            "The pitch of the background hum shifts. It's working.");
        coilsAligned.addChoice(new Choice("BACK", "Return to console", SCENE_LAB_HUB));
        storyScenes.put(SCENE_COILS_ALIGNED, coilsAligned);

        // 4. The Horror Reveal
        Scene readJournal = new Scene(SCENE_READ_JOURNAL,
            "October 14th. The theory holds. The resonance is stable at 432Hz. But the silence inside the portal... " +
            "it isn't empty. I looked into the waveform. I heard something calling back. It sounds like her. " +
            "I turned it off immediately. I am afraid I wasn't fast enough. Something got through. It's in the corner.");
        readJournal.addChoice(new Choice("TOUCH_STAIN", "Touch the dark stain on the page", SCENE_FLASHBACK));
        readJournal.addChoice(new Choice("BACK", "Close the book", SCENE_LAB_HUB));
        storyScenes.put(SCENE_READ_JOURNAL, readJournal);

        Scene flashback = new Scene(SCENE_FLASHBACK,
            "[VIDEO LOG PLAYBACK START]\n\n" +
            "The camera shakes. Dr. Vane is panning to the corner. 'It's not rendering correctly!' he screams. " +
            "In the corner of the room, reality is pixelating. A shape made of wireframe darkness lunges at the lens.\n\n" +
            "[PLAYBACK END]",
            "https://placehold.co/600x400/111111/FF0000?text=CORRUPTED+DATA" // Visual Payoff
        );
        flashback.addChoice(new Choice("BACK", "Gasps and step back", SCENE_LAB_HUB));
        storyScenes.put(SCENE_FLASHBACK, flashback);

        // 5. The Entity
        Scene entityAppears = new Scene(SCENE_ENTITY_APPEARS,
            "You finish the sequence. The PC reads: READY TO ENGAGE.\n\n" +
            "Suddenly, the light in the hallway flickers and dies. The temperature drops 20 degrees. " +
            "A high-pitched screeching noise tears through the air. In the corner... the darkness is expanding. " +
            "It deletes the filing cabinet. It's not moving; it's overwriting reality.",
            "https://placehold.co/600x400/000000/333333?text=THE+VOID"
        );
        entityAppears.addChoice(new Choice("FIGHT", "Throw the wrench at it", SCENE_ENTITY_DEATH));
        entityAppears.addChoice(new Choice("ACTIVATE", "INITIATE SEQUENCE (Run to terminal)", SCENE_ACTIVATE_PORTAL));
        storyScenes.put(SCENE_ENTITY_APPEARS, entityAppears);

        Scene entityDeath = new Scene(SCENE_ENTITY_DEATH,
            "You throw the wrench. It enters the Void and vanishes instantly. No sound. No impact. " +
            "The entity creates a wireframe appendage and swipes. Your vision turns into static. You have been deleted. GAME OVER.",
            true
        );
        storyScenes.put(SCENE_ENTITY_DEATH, entityDeath);

        Scene activatePortal = new Scene(SCENE_ACTIVATE_PORTAL,
            "You scramble to the terminal. You slam the Enter key. " +
            "The coils scream. The room shakes violently. In the center of the room, the air splits open. " +
            "It's not a hole, but a shimmering wall of distorted light. The Entity screams—a garbled, digital noise—and lunges."
        );
        activatePortal.addChoice(new Choice("JUMP", "JUMP INTO THE PORTAL", SCENE_JUMP_PORTAL));
        storyScenes.put(SCENE_ACTIVATE_PORTAL, activatePortal);

        Scene jumpPortal = new Scene(SCENE_JUMP_PORTAL,
            "You dive. As you pass the threshold, you look back. The Entity stops. It watches you go. " +
            "Did it... smile?\n\n" +
            "Everything goes white. You are falling through a tunnel of light and sound. " +
            "To be continued in Chapter 2...",
            "https://placehold.co/600x400/FFFFFF/000000?text=CHAPTER+2"
        );
        jumpPortal.setEndingScene(true);
        storyScenes.put(SCENE_JUMP_PORTAL, jumpPortal);
    }

    @Transactional
    public GameSession startGame(String playerName) {
        Optional<Player> existingPlayerOpt = playerRepository.findByName(playerName);
        Player player;

        if (existingPlayerOpt.isPresent()) {
            player = existingPlayerOpt.get();
            // Reset Flags
            player.setCoilsAligned(false);
            player.setCoolingSystemFixed(false);
            player.setJournalRead(false);
            player.setNerve(100);
            player = playerRepository.save(player);
        } else {
            player = new Player(playerName);
            player = playerRepository.save(player);
        }

        GameSession session = new GameSession(player, SCENE_LOGIN_TERMINAL);
        return gameSessionRepository.save(session);
    }

    @Transactional
    public GameSession processChoice(Long sessionId, String choiceId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        Player player = session.getPlayer();
        String currentSceneId = session.getCurrentSceneId();
        String nextSceneId = null;

        // --- Logic for Dynamic State Changes ---

        if (currentSceneId.equals(SCENE_INSPECT_COILS) && choiceId.equals("ALIGN_COILS")) {
            player.setCoilsAligned(true);
            player.setRigging(player.getRigging() + 1); // Stat increase
            playerRepository.save(player);
            nextSceneId = SCENE_COILS_ALIGNED;
        }
        else if (currentSceneId.equals(SCENE_READ_JOURNAL) && choiceId.equals("TOUCH_STAIN")) {
            player.setJournalRead(true);
            player.setNerve(player.getNerve() - 10); // Sanity damage
            playerRepository.save(player);
            nextSceneId = SCENE_FLASHBACK;
        }
        else if (currentSceneId.equals(SCENE_LAB_HUB)) {
            // Check if player has done everything to trigger the horror
            if (player.isCoilsAligned() && player.isJournalRead()) {
                // If they try to do anything else, trigger the entity
                nextSceneId = SCENE_ENTITY_APPEARS;
            }
        }

        // Fallback to standard choice navigation if logic didn't catch it
        if (nextSceneId == null) {
            Scene currentScene = storyScenes.get(currentSceneId);
            Choice choice = currentScene.getChoices().stream()
                    .filter(c -> c.getId().equals(choiceId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid choice"));
            nextSceneId = choice.getNextSceneId();
        }

        session.setCurrentSceneId(nextSceneId);

        // Handle Ending
        Scene nextScene = storyScenes.get(nextSceneId);
        if (nextScene.isEndingScene()) {
            session.setGameOver(true);
            session.setGameOutcomeMessage(nextScene.getDescription());
        }

        return gameSessionRepository.save(session);
    }

    public Scene getSceneDetails(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId).orElseThrow();
        Player player = session.getPlayer();
        Scene sceneDef = storyScenes.get(session.getCurrentSceneId());

        // Create a copy to modify choices dynamically
        Scene displayScene = new Scene(sceneDef.getId(), sceneDef.getDescription(), sceneDef.getImageUrl());

        // Dynamic Choices for HUB
        if (session.getCurrentSceneId().equals(SCENE_LAB_HUB)) {
            if (!player.isCoilsAligned()) {
                displayScene.addChoice(new Choice("INSPECT_COILS", "Inspect Copper Coils", SCENE_INSPECT_COILS));
            }
            if (!player.isJournalRead()) {
                displayScene.addChoice(new Choice("READ_JOURNAL", "Read Dusty Journal", SCENE_READ_JOURNAL));
            }
            // Trigger the ending sequence if tasks are done
            if (player.isCoilsAligned() && player.isJournalRead()) {
                 displayScene.setDescription("You have aligned the coils and reviewed the warnings. The PC terminal blinks: READY TO ENGAGE.");
                 displayScene.addChoice(new Choice("ENGAGE", "Engage System", SCENE_ENTITY_APPEARS));
            }
        } else {
            // Standard choices copy
            displayScene.setChoices(new ArrayList<>(sceneDef.getChoices()));
        }

        return displayScene;
    }
}
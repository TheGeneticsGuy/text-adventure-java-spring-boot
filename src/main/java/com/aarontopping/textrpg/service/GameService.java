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

    // --- SCENE IDS ---
    public static final String SCENE_LOGIN = "SCENE_LOGIN";
    public static final String SCENE_LAB_ENTRANCE = "SCENE_LAB_ENTRANCE";
    public static final String SCENE_LAB_HUB = "SCENE_LAB_HUB";
    
    // Tasks
    public static final String SCENE_INSPECT_COILS = "SCENE_INSPECT_COILS";
    public static final String SCENE_FIX_COILS = "SCENE_FIX_COILS";
    public static final String SCENE_INSPECT_COOLING = "SCENE_INSPECT_COOLING";
    public static final String SCENE_FIX_COOLING = "SCENE_FIX_COOLING";
    
    // Horror / Lore
    public static final String SCENE_READ_JOURNAL = "SCENE_READ_JOURNAL";
    public static final String SCENE_FLASHBACK = "SCENE_FLASHBACK";
    
    // Climax
    public static final String SCENE_ACCESS_PC = "SCENE_ACCESS_PC";
    public static final String SCENE_ENTITY_ARRIVAL = "SCENE_ENTITY_ARRIVAL";
    public static final String SCENE_ENTITY_DEATH = "SCENE_ENTITY_DEATH";
    public static final String SCENE_ACTIVATE_PORTAL = "SCENE_ACTIVATE_PORTAL";
    public static final String SCENE_JUMP = "SCENE_JUMP";

    @Autowired
    public GameService(PlayerRepository playerRepository, GameSessionRepository gameSessionRepository) {
        this.playerRepository = playerRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    @PostConstruct
    public void initializeStory() {
        storyScenes.clear();

        // 1. The Boot Sequence
        Scene login = new Scene(SCENE_LOGIN,
            "OS v9.4.2 BOOT SEQUENCE INITIATED...\n\n" +
            "Loading Kernel... OK.\n" +
            "Mounting Volume: 'Bell-Resonance'... OK.\n" +
            "Decrypting User Profile... OK.\n" +
            "Establishing Biometric Link... OK.\n\n" +
            "SYSTEM READY."
        );
        login.addChoice(new Choice("ENTER", "Begin Session", SCENE_LAB_ENTRANCE));
        storyScenes.put(SCENE_LOGIN, login);

        // 2. The Setup / Entrance
        Scene entrance = new Scene(SCENE_LAB_ENTRANCE,
            "You stand at the door of Lab 4. You shouldn't be here this late, but the rumors about Dr. Vane's work were too loud to ignore. " +
            "They say he found a way to use sound waves to push matter, creating some kind of 'Quantum-Cymatic Entanglement.' It was only rumors before he disappeared, but you had to know. "  +
            "The door to his old lab was oddly, unlocked tonight, and the door cracked. As you near the entrance, the air inside smells of ozone, copper, and something... stale. Like air that hasn't moved in a century."
        );
        entrance.addChoice(new Choice("ENTER_LAB", "Enter the Lab", SCENE_LAB_HUB));
        storyScenes.put(SCENE_LAB_ENTRANCE, entrance);

        // 3. The Hub (Central processing)
        Scene hub = new Scene(SCENE_LAB_HUB,
            "The lab is a chaotic mess of cables and analog equipment. " +
            "In the center sits a strange Array: massive copper coils pointed at a vacuum chamber. " +
            "To your left, a Cooling System hums smoothly, slightly vibrating the floor. That's odd. Dr. Vane had been missing for months, why would the equipment be on? " +
            "To your right, the Main Control PC sits waiting. " +
            "As you clear off the cluttered desk, underneath all the mess you find a grey, dusty journal sitting open."
        );
        // Choices are dynamic in getSceneDetails based on what is fixed
        storyScenes.put(SCENE_LAB_HUB, hub);

        // 4. Task: Coils
        Scene inspectCoils = new Scene(SCENE_INSPECT_COILS,
            "You examine the Emitter Coils. They are misaligned by exactly 15 degrees. " +
            "According to Vane's notes, the geometry must be perfect to create the standing wave. " +
            "A heavy wrench sits on the floor nearby."
        );
        inspectCoils.addChoice(new Choice("FIX_COILS", "Use Wrench to Rotate Coils (Requires Rigging)", SCENE_FIX_COILS));
        inspectCoils.addChoice(new Choice("BACK", "Return to Hub", SCENE_LAB_HUB));
        storyScenes.put(SCENE_INSPECT_COILS, inspectCoils);

        Scene fixCoils = new Scene(SCENE_FIX_COILS,
            "You heave on the wrench. Metal screeches against metal. With a final CLANG, the coil locks into place. " +
            "The ambient hum in the room shifts pitch immediately. It resonates in your teeth. " +
            "You feel... watched."
        );
        fixCoils.addChoice(new Choice("BACK", "Return to Hub", SCENE_LAB_HUB));
        storyScenes.put(SCENE_FIX_COILS, fixCoils);

        // 5. Task: Cooling
        Scene inspectCooling = new Scene(SCENE_INSPECT_COOLING,
            "The Quantum Processors are overheating. The nitrogen flow is blocked. " +
            "You can try to hack the university grid to reroute power for active cooling, or try to manually force the liquid nitrogen valve."
        );
        inspectCooling.addChoice(new Choice("FIX_COOLING_MANUAL", "Force the Valve (Requires Nerve)", SCENE_FIX_COOLING));
        inspectCooling.addChoice(new Choice("BACK", "Return to Hub", SCENE_LAB_HUB));
        storyScenes.put(SCENE_INSPECT_COOLING, inspectCooling);

        Scene fixCooling = new Scene(SCENE_FIX_COOLING,
            "You grab the freezing valve wheel. It burns your skin with cold. You grit your teeth and turn. " +
            "HISS! A plume of white vapor erupts, and the angry vibration of the machine smooths out into a steady purr. " +
            "System temperature stabilizing."
        );
        fixCooling.addChoice(new Choice("BACK", "Return to Hub", SCENE_LAB_HUB));
        storyScenes.put(SCENE_FIX_COOLING, fixCooling);

        // 6. The Journal / Flashback
        Scene journal = new Scene(SCENE_READ_JOURNAL,
            "October 14th.\nThe theory holds. The Choir is singing. The resonance is stable at 432Hz. What is this vortex that appeared? It looks like a portal. " +
            "But the silence inside the portal... it isn't empty. I looked into the waveform. " +
            "I heard something calling back. It sounds like [REDACTED]. I turned it off immediately. " +
            "I am afraid I wasn't fast enough. Something got through. It's in the corner. It's..." +
            "\n\n(There is a dark, oily stain on the page.)"
        );
        journal.addChoice(new Choice("TOUCH_STAIN", "Touch the stain", SCENE_FLASHBACK));
        journal.addChoice(new Choice("BACK", "Close the book", SCENE_LAB_HUB));
        storyScenes.put(SCENE_READ_JOURNAL, journal);

        Scene flashback = new Scene(SCENE_FLASHBACK,
            "[VISUAL RECORDING STARTED]\n\n" +
            "Your vision blurs. You aren't in the lab anymore—you are watching a recording. The camera shakes violently. " +
            "Dr. Vane is holding it. 'It's not rendering correctly!' he screams. " +
            "He pans the camera to the corner of the room. Reality there is pixelating, like a corrupted JPEG file in 3D space. " +
            "A shape made of wireframe darkness lunges at the lens. \n\n[RECORDING ENDED]",
            "https://placehold.co/600x400/111111/AA0000?text=CORRUPTED+DATA" // Placeholder for visual
        );
        flashback.addChoice(new Choice("BACK", "Gasp and step back", SCENE_LAB_HUB));
        storyScenes.put(SCENE_FLASHBACK, flashback);

        // 7. The PC / Trigger
        Scene accessPC = new Scene(SCENE_ACCESS_PC,
            "You sit at the terminal. With the coils aligned and cooling fixed, the screen glows green.\n\n" +
            "COMMAND LINE: READY TO ENGAGE.\n\n" +
            "You hover your finger over the Enter key. A feeling of dread washes over you."
        );
        accessPC.addChoice(new Choice("ENGAGE", "Execute Command: RUN PROTOCOL", SCENE_ENTITY_ARRIVAL));
        storyScenes.put(SCENE_ACCESS_PC, accessPC);

        // 8. The Entity
        Scene entityArrival = new Scene(SCENE_ENTITY_ARRIVAL,
            "You hit Enter.\n\n" +
            "The light in the hallway flickers and dies. The temperature drops 20 degrees instantly. " +
            "A high-pitched screeching noise tears through the air. " +
            "In the corner... a Vantablack Void appears. It doesn't walk. It expands. " +
            "It deletes the filing cabinet. Not moved. Gone. The darkness ate it.\n\n" +
            "Wireframe appendages reach out.",
            "https://placehold.co/600x400/000000/333333?text=THE+VOID"
        );
        entityArrival.addChoice(new Choice("FIGHT", "Throw the wrench at it", SCENE_ENTITY_DEATH));
        entityArrival.addChoice(new Choice("ACTIVATE", "INITIATE SEQUENCE (Run to terminal)", SCENE_ACTIVATE_PORTAL));
        storyScenes.put(SCENE_ENTITY_ARRIVAL, entityArrival);

        Scene death = new Scene(SCENE_ENTITY_DEATH,
            "You throw the wrench. It enters the Void and vanishes instantly. No sound. No impact. " +
            "The Entity swipes a glitchy arm through your chest. You look down to see your body turning into static. " +
            "You have been deleted. GAME OVER.",
            true
        );
        storyScenes.put(SCENE_ENTITY_DEATH, death);

        // 9. The Climax
        Scene activatePortal = new Scene(SCENE_ACTIVATE_PORTAL,
            "You scramble back to the terminal, dodging a swipe. You type: > INITIATE SEQUENCE\n\n" +
            "The coils scream. The room shakes. The 'Portal' opens, yet it's not a hole, but a shimmering wall of distorted air in the center of the room. " +
            "The Entity screams a garbled, digital noise, and lunges for your throat."
        );
        activatePortal.addChoice(new Choice("JUMP", "JUMP INTO THE PORTAL", SCENE_JUMP));
        storyScenes.put(SCENE_ACTIVATE_PORTAL, activatePortal);

        Scene jump = new Scene(SCENE_JUMP,
            "You dive. As you pass the threshold, you look back. The Entity stops. It doesn't chase you. " +
            "It watches you go. Did it... smile?\n\n" +
            "The screen goes white. You are falling through a tunnel of light and sound. " +
            "CHAPTER 1 COMPLETE.",
            "https://placehold.co/600x400/FFFFFF/000000?text=CHAPTER+1+COMPLETE",
            true
        );
        storyScenes.put(SCENE_JUMP, jump);
    }

    @Transactional
    public GameSession startGame(String playerName) {
        Optional<Player> existingPlayerOpt = playerRepository.findByName(playerName);
        Player player;

        if (existingPlayerOpt.isPresent()) {
            player = existingPlayerOpt.get();
            
            // Check if this player already has a session (even a finished one)
           Optional<GameSession> oldSession = gameSessionRepository.findByPlayerId(player.getId());
            if (oldSession.isPresent()) {
                logger.info("Deleting old session for player: {}", player.getName());
                gameSessionRepository.delete(oldSession.get());
                gameSessionRepository.flush(); // Force delete now
            }
            // Reset story flags for the new run
            player.setCoilsAligned(false);
            player.setCoolingSystemFixed(false);
            player.setJournalRead(false);
            player.setNerve(100);
            player = playerRepository.save(player);
        } else {
            player = new Player(playerName);
            player = playerRepository.save(player);
        }

        // Now it is safe to create a new session because the old one is gone
        GameSession session = new GameSession(player, SCENE_LOGIN);
        return gameSessionRepository.save(session);
    }

    @Transactional
    public GameSession processChoice(Long sessionId, String choiceId) {
        GameSession session = gameSessionRepository.findById(sessionId).orElseThrow();
        Player player = session.getPlayer();
        String currentSceneId = session.getCurrentSceneId();
        String nextSceneId = null;

        // --- Logic for State Changes ---
        
        if (currentSceneId.equals(SCENE_INSPECT_COILS) && choiceId.equals("FIX_COILS")) {
            player.setCoilsAligned(true);
            player.setRigging(player.getRigging() + 1);
            playerRepository.save(player);
            nextSceneId = SCENE_FIX_COILS;
        } 
        else if (currentSceneId.equals(SCENE_INSPECT_COOLING) && choiceId.equals("FIX_COOLING_MANUAL")) {
            player.setCoolingSystemFixed(true);
            player.setNerve(player.getNerve() - 5);
            playerRepository.save(player);
            nextSceneId = SCENE_FIX_COOLING;
        }
        else if (currentSceneId.equals(SCENE_READ_JOURNAL) && choiceId.equals("TOUCH_STAIN")) {
            player.setJournalRead(true);
            playerRepository.save(player);
            nextSceneId = SCENE_FLASHBACK;
        }
        // --- NEW: Handle Dynamic Hub Navigation ---
        else if (currentSceneId.equals(SCENE_LAB_HUB)) {
            if (choiceId.equals("INSPECT_COILS")) nextSceneId = SCENE_INSPECT_COILS;
            else if (choiceId.equals("INSPECT_COOLING")) nextSceneId = SCENE_INSPECT_COOLING;
            else if (choiceId.equals("READ_JOURNAL")) nextSceneId = SCENE_READ_JOURNAL;
            else if (choiceId.equals("USE_PC")) nextSceneId = SCENE_ACCESS_PC; 
            else throw new IllegalArgumentException("Invalid Hub Choice");
        }

        // Standard Navigation (Fallback for static scenes)
        if (nextSceneId == null) {
            Scene currentScene = storyScenes.get(currentSceneId);
            Choice choice = currentScene.getChoices().stream()
                    .filter(c -> c.getId().equals(choiceId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid choice ID: " + choiceId));
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
        
        // Copy scene to modify choices dynamically
        Scene displayScene = new Scene(sceneDef.getId(), sceneDef.getDescription(), sceneDef.getImageUrl());
        
        // Dynamic Choices for HUB
        if (session.getCurrentSceneId().equals(SCENE_LAB_HUB)) {
            // Only show tasks if not done
            if (!player.isCoilsAligned()) {
                displayScene.addChoice(new Choice("INSPECT_COILS", "Inspect Copper Coils", SCENE_INSPECT_COILS));
            }
            if (!player.isCoolingSystemFixed()) {
                displayScene.addChoice(new Choice("INSPECT_COOLING", "Check Cooling System", SCENE_INSPECT_COOLING));
            }
            
            // Journal always available until read? Or always available.
            // Let's say Journal is always there.
            displayScene.addChoice(new Choice("READ_JOURNAL", "Read Dusty Journal", SCENE_READ_JOURNAL));

            // Check if ready for endgame
            if (player.isCoilsAligned() && player.isCoolingSystemFixed()) {
                 displayScene.setDescription(sceneDef.getDescription() + "\n\n[SYSTEM STATUS: GREEN. READY TO ENGAGE.]");
                 displayScene.addChoice(new Choice("USE_PC", "Access Control PC", SCENE_ACCESS_PC));
            }
        } else {
            // Standard copy for other scenes
            displayScene.setChoices(new ArrayList<>(sceneDef.getChoices()));
        }

        return displayScene;
    }
}
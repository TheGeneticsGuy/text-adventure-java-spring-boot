package com.aarontopping.textrpg.service;

import com.aarontopping.textrpg.model.*; // Import all models
import com.aarontopping.textrpg.repository.*; // Import all repositories
import com.aarontopping.textrpg.service.story.Choice;
import com.aarontopping.textrpg.service.story.Scene;
import jakarta.annotation.PostConstruct; // For initializing data
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GameService {

    private final PlayerRepository playerRepository;
    private final GameSessionRepository gameSessionRepository;
    private final SkillRepository skillRepository;
    private final CharacterClassRepository characterClassRepository;
    private final WeaponRepository weaponRepository;

    private final Map<String, Scene> storyScenes = new HashMap<>();

    // Skills - defining them as constants
    private Skill SKILL_PUNCH;
    private Skill SKILL_POWER_STRIKE;
    private Skill SKILL_AIMED_SHOT;

    // Character Classes
    private CharacterClass CLASS_WARRIOR;
    private CharacterClass CLASS_ARCHER;

    // Weapons
    private Weapon WEAPON_SWORD;
    private Weapon WEAPON_BOW;

    // Scene IDs
    public static final String START_SCENE_ID = "START";
    public static final String CLICK_LINK_CHOICE_SCENE_ID = "CLICK_LINK_CHOICE";
    public static final String TELEPORT_SCENE_ID = "TELEPORT_SCENE";
    public static final String CHOOSE_WEAPON_SCENE_ID = "CHOOSE_WEAPON"; // New scene
    public static final String FOREST_ENCOUNTER_ID = "FOREST_ENCOUNTER";
    public static final String END_COLLEGE_LIFE_ID = "END_COLLEGE_LIFE";
    public static final String FIND_PORTAL_HOME_ID = "FIND_PORTAL_HOME";
    public static final String ANOTHER_ADVENTURE_ID = "ANOTHER_ADVENTURE";
    public static final String GAME_OVER_RETURNED_ID = "GAME_OVER_RETURNED";
    public static final String MOCK_BATTLE_OUTCOME_ID = "MOCK_BATTLE_OUTCOME";

    @Autowired
    public GameService(PlayerRepository playerRepository, GameSessionRepository gameSessionRepository,
            SkillRepository skillRepository, CharacterClassRepository characterClassRepository,
            WeaponRepository weaponRepository) {
        this.playerRepository = playerRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.skillRepository = skillRepository;
        this.characterClassRepository = characterClassRepository;
        this.weaponRepository = weaponRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeGameData() {
        // --- Initialize Skills ---
        final Skill punchSkill = skillRepository.findByName("Punch")
                .orElseGet(() -> skillRepository.save(new Skill("Punch", "A basic hand-to-hand attack.", 5)));
        final Skill powerStrikeSkill = skillRepository.findByName("Power Slash")
                .orElseGet(() -> skillRepository.save(new Skill("Power Slash", "A strong melee attack.", 15)));
        final Skill aimedShotSkill = skillRepository.findByName("Ranged Arrow")
                .orElseGet(() -> skillRepository.save(new Skill("Ranged Arrow", "A precise ranged attack.", 15)));

        this.SKILL_PUNCH = punchSkill;
        this.SKILL_POWER_STRIKE = powerStrikeSkill;
        this.SKILL_AIMED_SHOT = aimedShotSkill;

        // --- Initialize Character Classes ---
        CLASS_WARRIOR = characterClassRepository.findByClassName("Warrior")
                .orElseGet(() -> {
                    Warrior warrior = new Warrior();
                    warrior.addDefaultSkill(this.SKILL_POWER_STRIKE);
                    return characterClassRepository.save(warrior); // Persist warrior
                });

        CLASS_ARCHER = characterClassRepository.findByClassName("Archer")
                .orElseGet(() -> {
                    Archer archer = new Archer();
                    archer.addDefaultSkill(this.SKILL_AIMED_SHOT);
                    return characterClassRepository.save(archer); // Persist archer
                });

        // --- Initialize Weapons ---
        WEAPON_SWORD = weaponRepository.findByName("Basic Sword")
                .orElseGet(() -> weaponRepository.save(new Weapon("Basic Sword", "SWORD", 3)));
        WEAPON_BOW = weaponRepository.findByName("Basic Bow")
                .orElseGet(() -> weaponRepository.save(new Weapon("Basic Bow", "BOW", 3)));

        // --- Initialize Story Scenes ---
        initializeStory();
    }

    private void initializeStory() {
        storyScenes.clear(); // Clear on re-initialization

        // --- Layer 0: Introduction ---
        Scene start = new Scene(START_SCENE_ID,
                "You are a BYU student at the University of Idaho. Late one night, while working in the archives of the Computer Science department, you stumble upon some very old PC hardware. You find a computer unlike anything you have ever seen before. It looks old and modern at the same time. It feels warm to the touch, which is odd given it probably hasn't been plugged in for decades. Curious, you decided to plug it in and load it up. After a short moment, text appears in the middle of the screen with a cryptic link: 'Discover What Lies Beyond'.");
        start.addChoice(new Choice("1", "Investigate the link.", CLICK_LINK_CHOICE_SCENE_ID, null, false));
        start.addChoice(new Choice("2", "Ignore it and get back to work. You're tired anyway...", END_COLLEGE_LIFE_ID,
                null, false));
        storyScenes.put(START_SCENE_ID, start);

        // --- Layer 1: The Choice ---
        Scene clickLinkChoice = new Scene(CLICK_LINK_CHOICE_SCENE_ID,
                "Your Curiosity Draws you near. Your cursor hovers over the link. Should you do it?");
        clickLinkChoice.addChoice(new Choice("1", "Click the link!", TELEPORT_SCENE_ID, null, false));
        clickLinkChoice.addChoice(new Choice("2", "Nevermind, this is too weird. Close and get back to work.",
                END_COLLEGE_LIFE_ID, null, false));
        storyScenes.put(CLICK_LINK_CHOICE_SCENE_ID, clickLinkChoice);

        Scene endCollege = new Scene(END_COLLEGE_LIFE_ID,
                "You decide against it. College life continues. The mystery fades away forever. GAME OVER.", true);
        storyScenes.put(END_COLLEGE_LIFE_ID, endCollege);

        // --- Layer 2: Into the New World & Weapon Choice ---
        Scene teleport = new Scene(TELEPORT_SCENE_ID,
                "A blinding flash occurs! You feel your body lift from the ground. You feel weightless. In an instant, you land in a dim forest. Where did you go? Are you dreaming!? The air is strange. Before you, on a mossy stone, lie two items: a sturdy-looking sword and a well-crafted bow.");
        teleport.addChoice(new Choice("SWORD", "Take the Sword.", CHOOSE_WEAPON_SCENE_ID, null, false)); // Choice ID
        teleport.addChoice(new Choice("BOW", "Take the Bow.", CHOOSE_WEAPON_SCENE_ID, null, false));
        storyScenes.put(TELEPORT_SCENE_ID, teleport);

        Scene chooseWeaponOutcome = new Scene(CHOOSE_WEAPON_SCENE_ID,
                "You've made your choice. You feel a new sense of purpose... and hear a rustling in the bushes nearby.",
                false);

        // Let's build a generic encounter - only once choice
        chooseWeaponOutcome
                .addChoice(new Choice("CONTINUE", "Investigate the rustling.", FOREST_ENCOUNTER_ID, null, false));
        storyScenes.put(CHOOSE_WEAPON_SCENE_ID, chooseWeaponOutcome);

        // --- Layer 3: First Encounter (will now use skills) ---
        Scene forestEncounter = new Scene(FOREST_ENCOUNTER_ID,
                "A shadowy creature unlike anything you have ever seen emerges! Slimy, towering creature at least 8 feet tall stands before you... fixated on you. I doesn't look friendly. It's teeth razor sharp, it's lips curling. It looks hostile. What do you do?");

        // We will dynamically add skill choices + a "Flee" option.
        forestEncounter.setBattleScene(true); // Mark as battle scene to trigger dynamic choice generation
        storyScenes.put(FOREST_ENCOUNTER_ID, forestEncounter);

        Scene mockBattleOutcome = new Scene(MOCK_BATTLE_OUTCOME_ID,
                "After your swift action, the creature has been neutralized! That was close! The path ahead seems clearer.");
        mockBattleOutcome.addChoice(new Choice("1", "Continue exploring.", FIND_PORTAL_HOME_ID, null, false));
        storyScenes.put(MOCK_BATTLE_OUTCOME_ID, mockBattleOutcome);

        // --- Endings ---
        Scene findPortal = new Scene(FIND_PORTAL_HOME_ID,
                "As you travel down the dimly lit path, a clearing appears. There is a path to your left, to your right, and right ahead of you is some kind of shimmering portal. It feels familiar... This must be the way back!");
        findPortal.addChoice(
                new Choice("1", "Step through the portal to return home.", GAME_OVER_RETURNED_ID, null, false));
        findPortal.addChoice(
                new Choice("2", "Stay and explore this new world further.", ANOTHER_ADVENTURE_ID, null, false));
        storyScenes.put(FIND_PORTAL_HOME_ID, findPortal);

        Scene gameOverReturned = new Scene(GAME_OVER_RETURNED_ID,
                "You're back in the Computer Science building. The website on the monitor states '404 Not Found'. Did I just fall asleep and dream this? GAME OVER.",
                true);
        storyScenes.put(GAME_OVER_RETURNED_ID, gameOverReturned);

        Scene anotherAdventure = new Scene(ANOTHER_ADVENTURE_ID,
                "This world is too fascinating. Another adventure begins! (Further story TBA!!). GAME OVER.",
                true);
        storyScenes.put(ANOTHER_ADVENTURE_ID, anotherAdventure);
    }

    @Transactional
    public GameSession startGame(String playerName) {
        Player player = playerRepository.findByName(playerName).orElseGet(() -> {
            Player newPlayer = new Player(playerName);
            // Add common skills to all new players
            newPlayer.addSkill(SKILL_PUNCH);
            return playerRepository.save(newPlayer);
        });

        // If player already exists but has no class
        if (player.getCharacterClass() == null) {
            player.getKnownSkills().clear();
            player.addSkill(SKILL_PUNCH);
        }

        Optional<GameSession> existingSession = gameSessionRepository.findByPlayerIdAndGameOver(player.getId(), false);
        if (existingSession.isPresent()) {
            GameSession session = existingSession.get();
            // If game is over, or if player has no class yet (meaning they are at weapon
            // choice or before)
            if (session.isGameOver() || player.getCharacterClass() == null) {
                // Force new session if player needs to pick class again or game was over
                GameSession newGameSession = new GameSession(player, START_SCENE_ID);
                return gameSessionRepository.save(newGameSession);
            }
            return session; // Return to existing game
        }

        GameSession newGameSession = new GameSession(player, START_SCENE_ID);
        return gameSessionRepository.save(newGameSession);
    }

    @Transactional
    public GameSession processChoice(Long sessionId, String choiceId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + sessionId));
        Player player = session.getPlayer();

        if (session.isGameOver()) {
            throw new IllegalStateException("Game is already over.");
        }

        Scene currentScene = storyScenes.get(session.getCurrentSceneId());
        if (currentScene == null) {
            throw new IllegalStateException("Unknown scene ID: " + session.getCurrentSceneId());
        }

        String nextSceneId = null;

        // Handle weapon choice / class assignment
        if (session.getCurrentSceneId().equals(TELEPORT_SCENE_ID)) {
            if (choiceId.equals("SWORD")) {
                player.setCharacterClass(CLASS_WARRIOR);
                player.setEquippedWeapon(weaponRepository.findByName("Basic Sword").orElse(WEAPON_SWORD));
                player.addSkills(CLASS_WARRIOR.getDefaultSkills());
                nextSceneId = CHOOSE_WEAPON_SCENE_ID;
            } else if (choiceId.equals("BOW")) {
                player.setCharacterClass(CLASS_ARCHER);
                player.setEquippedWeapon(weaponRepository.findByName("Basic Bow").orElse(WEAPON_BOW));
                player.addSkills(CLASS_ARCHER.getDefaultSkills());
                nextSceneId = CHOOSE_WEAPON_SCENE_ID;
            } else {
                throw new IllegalArgumentException("Invalid weapon choice.");
            }
            playerRepository.save(player);
        } else if (currentScene.isBattleScene()) {
            // If it's a battle scene, the choiceId will b e a skill name
            Skill chosenSkill = player.getKnownSkills().stream()
                    .filter(s -> s.getName().replaceAll("\\s+", "_").equalsIgnoreCase(choiceId))
                    .findFirst().orElse(null);
            if (chosenSkill != null) {
                // For a real battle, you'd calculate damage, enemy response, etc.
                session.setGameOutcomeMessage("You used " + chosenSkill.getName() + "!");
                nextSceneId = MOCK_BATTLE_OUTCOME_ID;
            } else if (choiceId.equals("FLEE")) {
                nextSceneId = FIND_PORTAL_HOME_ID; // Example: Fleeing leads to finding the portal out!
                session.setGameOutcomeMessage("You managed to flee!");
            } else {
                throw new IllegalArgumentException("Invalid action in battle: " + choiceId);
            }

        } else {
            // Standard choices processing is here... with errors
            Choice chosen = currentScene.getChoices().stream()
                    .filter(c -> c.getId().equals(choiceId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid choice ID: " + choiceId + " for scene " + currentScene.getId()));
            nextSceneId = chosen.getNextSceneId();
        }

        if (nextSceneId == null) {
            throw new IllegalStateException("Next scene could not be determined."); // Just debugging tracing.
        }
        session.setCurrentSceneId(nextSceneId);

        Scene nextSceneDetails = storyScenes.get(nextSceneId);
        if (nextSceneDetails.isEndingScene()) {
            session.setGameOver(true);
            if (session.getGameOutcomeMessage() == null || session.getGameOutcomeMessage().isEmpty()) {
                session.setGameOutcomeMessage(nextSceneDetails.getDescription());
            }
        }

        return gameSessionRepository.save(session);
    }

    // This is to help me build DYNAMIC battle scenes.
    public Scene getSceneDetails(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Game session not found for scene details: " + sessionId));
        Player player = session.getPlayer();
        Scene originalScene = storyScenes.get(session.getCurrentSceneId());

        if (originalScene == null) {
            throw new IllegalArgumentException("Scene not found: " + session.getCurrentSceneId());
        }

        Scene sceneForDisplay = new Scene(originalScene.getId(), originalScene.getDescription());
        sceneForDisplay.setBattleScene(originalScene.isBattleScene());
        sceneForDisplay.setEndingScene(originalScene.isEndingScene());

        if (originalScene.isBattleScene() && player.getCharacterClass() != null) {
            // Dynamically add skill choices for battle scenes
            player.getKnownSkills().forEach(skill -> {
                String choiceId = skill.getName().replaceAll("\\s+", "_");
                sceneForDisplay.addChoice(
                        new Choice(choiceId, "Use " + skill.getName(), MOCK_BATTLE_OUTCOME_ID, skill.getName(), true));
            });
            sceneForDisplay.addChoice(new Choice("FLEE", "Try to Flee", FIND_PORTAL_HOME_ID, null, true));
        } else {
            // For non-battle scenes, we will use predefined choices
            sceneForDisplay.setChoices(new ArrayList<>(originalScene.getChoices()));
        }
        return sceneForDisplay;
    }

    public Optional<GameSession> getGameSession(Long sessionId) {
        return gameSessionRepository.findById(sessionId);
    }
}
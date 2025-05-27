package com.aarontopping.textrpg.service;

import com.aarontopping.textrpg.model.*;
import com.aarontopping.textrpg.repository.*;
import com.aarontopping.textrpg.service.story.Choice;
import com.aarontopping.textrpg.service.story.Scene;
import jakarta.annotation.PostConstruct;
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
    private final WeaponTemplateRepository weaponTemplateRepository;

    private final Map<String, Scene> storyScenes = new HashMap<>();

    // Skills
    private Skill SKILL_PUNCH;
    private Skill SKILL_POWER_STRIKE;
    private Skill SKILL_AIMED_SHOT;

    // Character Classes
    private CharacterClass CLASS_WARRIOR;
    private CharacterClass CLASS_ARCHER;

    // Weapon Templates
    private WeaponTemplate WEAPON_SWORD_TEMPLATE;
    private WeaponTemplate WEAPON_BOW_TEMPLATE;

    // Scene IDs
    public static final String START_SCENE_ID = "START";
    public static final String CLICK_LINK_CHOICE_SCENE_ID = "CLICK_LINK_CHOICE";
    public static final String TELEPORT_SCENE_ID = "TELEPORT_SCENE";

    // player choice
    public static final String CHOOSE_WARRIOR_CLASS_ID = "CHOOSE_WARRIOR_CLASS";
    public static final String CHOOSE_ARCHER_CLASS_ID = "CHOOSE_ARCHER_CLASS";
    public static final String FOREST_ENCOUNTER_ID = "FOREST_ENCOUNTER"; // Initial battle scene
    public static final String FOREST_ENCOUNTER_PUNCHED_ONCE_ID = "FOREST_ENCOUNTER_PUNCHED_ONCE";
    public static final String FOREST_ENCOUNTER_PLAYER_DEFEATED_ID = "FOREST_ENCOUNTER_PLAYER_DEFEATED";
    public static final String FOREST_ENCOUNTER_GNURR_DEFEATED_ID = "FOREST_ENCOUNTER_GNURR_DEFEATED";
    public static final String FOREST_ENCOUNTER_FLED_ID = "FOREST_ENCOUNTER_FLED";
    public static final String FIND_PORTAL_HOME_ID = "FIND_PORTAL_HOME";
    public static final String END_COLLEGE_LIFE_ID = "END_COLLEGE_LIFE";
    public static final String ANOTHER_ADVENTURE_ID = "ANOTHER_ADVENTURE";
    public static final String GAME_OVER_RETURNED_ID = "GAME_OVER_RETURNED";

    // Key Selection or not Scene
    public static final String KEY_TAKEN_SCENE_ID = "KEY_TAKEN_SCENE";
    public static final String KEY_LEFT_SCENE_ID = "KEY_LEFT_SCENE";

    @Autowired
    public GameService(PlayerRepository playerRepository, GameSessionRepository gameSessionRepository,
            SkillRepository skillRepository, CharacterClassRepository characterClassRepository,
            WeaponTemplateRepository weaponTemplateRepository) {
        this.playerRepository = playerRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.skillRepository = skillRepository;
        this.characterClassRepository = characterClassRepository;
        this.weaponTemplateRepository = weaponTemplateRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeGameData() {
        final Skill punchSkill = skillRepository.findByName("Punch")
                .orElseGet(() -> skillRepository.save(new Skill("Punch", "A basic hand-to-hand attack.", 5)));
        final Skill powerStrikeSkill = skillRepository.findByName("Power Slash")
                .orElseGet(() -> skillRepository.save(new Skill("Power Slash", "A strong melee attack.", 15)));
        final Skill aimedShotSkill = skillRepository.findByName("Ranged Arrow")
                .orElseGet(() -> skillRepository.save(new Skill("Ranged Arrow", "A precise ranged attack.", 15)));

        this.SKILL_PUNCH = punchSkill;
        this.SKILL_POWER_STRIKE = powerStrikeSkill;
        this.SKILL_AIMED_SHOT = aimedShotSkill;

        CLASS_WARRIOR = characterClassRepository.findByClassName("Warrior")
                .orElseGet(() -> {
                    Warrior warrior = new Warrior();
                    warrior.addDefaultSkill(this.SKILL_POWER_STRIKE);
                    return characterClassRepository.save(warrior);
                });

        CLASS_ARCHER = characterClassRepository.findByClassName("Archer")
                .orElseGet(() -> {
                    Archer archer = new Archer();
                    archer.addDefaultSkill(this.SKILL_AIMED_SHOT);
                    return characterClassRepository.save(archer);
                });

        WEAPON_SWORD_TEMPLATE = weaponTemplateRepository.findByName("Basic Sword") // Use renamed field
                .orElseGet(() -> weaponTemplateRepository.save(new WeaponTemplate("Basic Sword", "SWORD", 3)));
        WEAPON_BOW_TEMPLATE = weaponTemplateRepository.findByName("Basic Bow") // Use renamed field
                .orElseGet(() -> weaponTemplateRepository.save(new WeaponTemplate("Basic Bow", "BOW", 3)));

        initializeStory();
    }

    private void initializeStory() {
        storyScenes.clear();

        Scene start = new Scene(START_SCENE_ID,
                "You are a BYU student at the University of Idaho. Late one night, while working in the archives of the Computer Science department, you stumble upon some very old PC hardware. You find a computer unlike anything you have ever seen before. It looks old and modern at the same time. It feels warm to the touch, which is odd given it probably hasn't been plugged in for decades. Curious, you bring it back to your dorm and decide to plug it in and load it up. After a short moment, text appears in the middle of the screen with a cryptic link: 'Discover What Lies Beyond'.");
        start.addChoice(new Choice("1", "Investigate the link.", CLICK_LINK_CHOICE_SCENE_ID, null, false));
        start.addChoice(new Choice("2", "Ignore it and get back to work. You're tired anyway...", END_COLLEGE_LIFE_ID,
                null, false));
        storyScenes.put(START_SCENE_ID, start);

        Scene clickLinkChoice = new Scene(CLICK_LINK_CHOICE_SCENE_ID,
                "Your Curiosity Draws you near. Your cursor hovers over the link. Should you do it?");
        clickLinkChoice.addChoice(new Choice("1", "Click the link!", TELEPORT_SCENE_ID, null, false));
        clickLinkChoice.addChoice(new Choice("2", "Nevermind, this is too weird. Close and get back to work.",
                END_COLLEGE_LIFE_ID, null, false));
        storyScenes.put(CLICK_LINK_CHOICE_SCENE_ID, clickLinkChoice);

        Scene endCollege = new Scene(END_COLLEGE_LIFE_ID,
                "You decide against it. College life continues. The mystery fades away forever. GAME OVER.", true);
        storyScenes.put(END_COLLEGE_LIFE_ID, endCollege);

        Scene teleport = new Scene(TELEPORT_SCENE_ID,
                "A blinding flash occurs! You feel your body lift from the ground. You feel weightless. In an instant, you land in a dim forest. Where did you go? Are you dreaming!? The air is strange. Before you, on a mossy stone, lie two items: a sturdy-looking sword and a well-crafted bow.");
        teleport.addChoice(new Choice("SWORD", "Take the Sword.", null, null, false)); // Next scene determined in
                                                                                       // processChoice
        teleport.addChoice(new Choice("BOW", "Take the Bow.", null, null, false)); // Next scene determined in
                                                                                   // processChoice
        storyScenes.put(TELEPORT_SCENE_ID, teleport);

        Scene warriorChosen = new Scene(CHOOSE_WARRIOR_CLASS_ID,
                "The cold steel of the sword feels strangely familiar in your grasp. It's heavier than you expected, yet balanced. A surge of forgotten strength, of primal instincts, flows through you. In this world, the analytical mind of a coder gives way to something older, something fiercer. Perhaps in another age, your destiny would not have been lines of code, but standing in lines on a battlefield. You are a Warrior. You feel a new sense of purpose... and hear a rustling in the bushes nearby.");
        warriorChosen.addChoice(new Choice("CONTINUE", "Acknowledge your new path and investigate the rustling.",
                FOREST_ENCOUNTER_ID, null, false));
        storyScenes.put(CHOOSE_WARRIOR_CLASS_ID, warriorChosen);

        Scene archerChosen = new Scene(CHOOSE_ARCHER_CLASS_ID,
                "The polished wood of the bow hums faintly as you pick it up, the string taut and ready. Your fingers instinctively find their place. A sense of keen focus, of distant targets and trajectories, sharpens your mind. The digital precision you honed in computer science translates into an uncanny aim. Maybe the path of logic and algorithms you walked in your old life was but a reflection of this archer's instinct, now awakened. You are an Archer. You feel a new sense of purpose... and hear a rustling in the bushes nearby.");
        archerChosen.addChoice(new Choice("CONTINUE", "Embrace your newfound skill and investigate the rustling.",
                FOREST_ENCOUNTER_ID, null, false));
        storyScenes.put(CHOOSE_ARCHER_CLASS_ID, archerChosen);

        Scene forestEncounter = new Scene(FOREST_ENCOUNTER_ID,
                "A shadowy creature unlike anything you have ever seen emerges! A slimy, towering creature at least 8 feet tall stands before you... fixated on you. It doesn't look friendly. Its teeth are razor sharp, its lips curling. It looks hostile. What do you do?");
        forestEncounter.setBattleScene(true);
        storyScenes.put(FOREST_ENCOUNTER_ID, forestEncounter);

        Scene forestEncounterPunchedOnce = new Scene(FOREST_ENCOUNTER_PUNCHED_ONCE_ID,
                "That probably wasn't the best idea. Your fist is now covered in a strange, sticky slime. The Grumbling Creature recoils for a moment, then lets out an enraged gurgle, its many eyes focusing on you with malice. It looms closer, angrier.");
        forestEncounterPunchedOnce.setBattleScene(true);
        storyScenes.put(FOREST_ENCOUNTER_PUNCHED_ONCE_ID, forestEncounterPunchedOnce);

        Scene playerDefeated = new Scene(FOREST_ENCOUNTER_PLAYER_DEFEATED_ID,
                "Before you can react to its fury, the Creature lunges with surprising speed. Its attack is overwhelming. Why didn't you use your weapon, you think to yourself as the darkness closes in... Your adventure ends here. GAME OVER.",
                true);
        storyScenes.put(FOREST_ENCOUNTER_PLAYER_DEFEATED_ID, playerDefeated);

        Scene gnurrDefeated = new Scene(FOREST_ENCOUNTER_GNURR_DEFEATED_ID,
                "With a final, wretched shriek, the towering creature shudders and dissolves into a pile of shimmering dust! Where the creature stood, a small, intricately carved iron key now rests on the forest floor. It looks ancient and important.");
        gnurrDefeated.addChoice(new Choice("TAKE_KEY", "Take the ancient iron key.", null, null, false));
        gnurrDefeated.addChoice(
                new Choice("LEAVE_KEY", "Leave the key. It's probably cursed.", null, null, false));
        storyScenes.put(FOREST_ENCOUNTER_GNURR_DEFEATED_ID, gnurrDefeated);

        Scene keyTaken = new Scene(KEY_TAKEN_SCENE_ID,
                "You reach down and pick up the iron key. It's surprisingly heavy, the metal cool and smooth against your palm, its intricate carvings worn with an unknowable age. A faint thrum of energy seems to emanate from it. You carefully tuck it into your pocket, a strange souvenir from this bizarre world. The path ahead is dimly lit.");
        keyTaken.addChoice(
                new Choice("CONTINUE_PATH", "Continue down the dimly lit path.", FIND_PORTAL_HOME_ID, null, false));
        storyScenes.put(KEY_TAKEN_SCENE_ID, keyTaken);

        Scene keyLeft = new Scene(KEY_LEFT_SCENE_ID,
                "A shiver runs down your spine. Touching something that was so close to that... creature... feels wrong. This world is alien, its rules unknown. Some mysteries are best left undisturbed. You turn away from the key, leaving it glinting on the forest floor. The path ahead is dimly lit.");
        keyLeft.addChoice(
                new Choice("CONTINUE_PATH", "Continue down the dimly lit path.", FIND_PORTAL_HOME_ID, null, false));
        storyScenes.put(KEY_LEFT_SCENE_ID, keyLeft);

        Scene forestFled = new Scene(FOREST_ENCOUNTER_FLED_ID,
                "Not chancing it, you turn and sprint as fast as your legs can carry you, deeper into the shadowed woods. Your heart pounds against your ribs, branches whip at your face, but you don't look back. Gradually, the monstrous sounds of the towering Creature fade behind you. You slow down, gasping for breath, seemingly safe... for now.");
        forestFled.addChoice(new Choice("CONTINUE_EXPLORING", "Catch your breath and continue exploring.",
                FIND_PORTAL_HOME_ID, null, false));
        storyScenes.put(FOREST_ENCOUNTER_FLED_ID, forestFled);

        Scene findPortal = new Scene(FIND_PORTAL_HOME_ID,
                "As you travel down the dimly lit path, a clearing appears. There is a path to your left, to your right, and right ahead of you is some kind of shimmering portal. It feels familiar... This must be the way back!");
        findPortal.addChoice(
                new Choice("1", "Step through the portal to return home.", GAME_OVER_RETURNED_ID, null, false));
        findPortal.addChoice(
                new Choice("2", "Stay and explore this new world further.", ANOTHER_ADVENTURE_ID, null, false));
        storyScenes.put(FIND_PORTAL_HOME_ID, findPortal);

        Scene gameOverReturned = new Scene(GAME_OVER_RETURNED_ID,
                "You're back in the Computer Science building. The website on the monitor states '404 Not Found'. Was it all a dream? GAME OVER.",
                true);
        storyScenes.put(GAME_OVER_RETURNED_ID, gameOverReturned);

        Scene anotherAdventure = new Scene(ANOTHER_ADVENTURE_ID,
                "This world is too fascinating. Another adventure begins! (Further story TBA!!). GAME OVER.", true);
        storyScenes.put(ANOTHER_ADVENTURE_ID, anotherAdventure);
    }

    @Transactional
    public GameSession startGame(String playerName) {
        Optional<Player> existingPlayerOpt = playerRepository.findByName(playerName);
        Player playerEntity;

        if (existingPlayerOpt.isPresent()) {
            playerEntity = existingPlayerOpt.get();

            List<GameSession> oldSessions = gameSessionRepository.findAllByPlayerId(playerEntity.getId());
            if (!oldSessions.isEmpty()) {
                gameSessionRepository.deleteAllInBatch(oldSessions); // More efficient for multiple deletions
                gameSessionRepository.flush();
            }

            playerEntity.setCharacterClass(null);
            playerEntity.setEquippedWeapon(null);
            playerEntity.setHasAncientKey(false); // Reset key status
            playerEntity.setHealth(100);

            List<Skill> newSkillList = new ArrayList<>();
            if (this.SKILL_PUNCH != null) {
                newSkillList.add(this.SKILL_PUNCH);
            }
            playerEntity.setKnownSkills(newSkillList); // Set to new list, effectively clearing old player-specific
                                                       // skills

            playerEntity = playerRepository.saveAndFlush(playerEntity); // Save and flush the reset state
        } else {
            Player newPlayer = new Player(playerName); // Constructor sets hasAncientKey to false
            if (this.SKILL_PUNCH != null) {
                newPlayer.addSkill(this.SKILL_PUNCH);
            }
            playerEntity = playerRepository.saveAndFlush(newPlayer);
        }

        GameSession newGameSession = new GameSession(playerEntity, START_SCENE_ID);
        GameSession savedNewGameSession = gameSessionRepository.saveAndFlush(newGameSession);
        return savedNewGameSession;
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
        session.setGameOutcomeMessage(null); // Clear previous transient outcome messages

        String currentSceneId = session.getCurrentSceneId();

        if (currentSceneId.equals(TELEPORT_SCENE_ID)) {
            WeaponTemplate weaponToEquip = null;
            CharacterClass classToAssign = null;

            if (choiceId.equals("SWORD")) {
                weaponToEquip = weaponTemplateRepository.findByName("Basic Sword")
                        .orElseThrow(() -> new IllegalStateException("Basic Sword template not found!"));
                classToAssign = characterClassRepository.findByClassName("Warrior")
                        .orElseThrow(() -> new IllegalStateException("Warrior class template not found!"));
                nextSceneId = CHOOSE_WARRIOR_CLASS_ID;
            } else if (choiceId.equals("BOW")) {
                weaponToEquip = weaponTemplateRepository.findByName("Basic Bow")
                        .orElseThrow(() -> new IllegalStateException("Basic Bow template not found!"));
                classToAssign = characterClassRepository.findByClassName("Archer")
                        .orElseThrow(() -> new IllegalStateException("Archer class template not found!"));
                nextSceneId = CHOOSE_ARCHER_CLASS_ID;
            } else {
                throw new IllegalArgumentException("Invalid weapon choice: " + choiceId);
            }

            player.setCharacterClass(classToAssign);
            if (classToAssign != null) { // Should always be non-null
                player.addSkills(classToAssign.getDefaultSkills());
            }
            player.setEquippedWeapon(weaponToEquip);
            playerRepository.saveAndFlush(player); // Save player changes immediately

        } else if (currentScene.isBattleScene()) {
            if (choiceId.equals(SKILL_PUNCH.getName().replaceAll("\\s+", "_"))) {
                if (currentSceneId.equals(FOREST_ENCOUNTER_ID)) {
                    nextSceneId = FOREST_ENCOUNTER_PUNCHED_ONCE_ID;
                    session.setGameOutcomeMessage("You punched the Creature. It's slimy and definitely angrier!");
                } else if (currentSceneId.equals(FOREST_ENCOUNTER_PUNCHED_ONCE_ID)) {
                    nextSceneId = FOREST_ENCOUNTER_PLAYER_DEFEATED_ID;
                    // The scene description for FOREST_ENCOUNTER_PLAYER_DEFEATED_ID will be the
                    // outcome.
                }
            } else if (player.getCharacterClass() != null &&
                    choiceId.equals(player.getCharacterClass().getPrimaryAttackSkillName().replaceAll("\\s+", "_"))) {
                // Used special attack
                if (currentSceneId.equals(FOREST_ENCOUNTER_ID)
                        || currentSceneId.equals(FOREST_ENCOUNTER_PUNCHED_ONCE_ID)) {
                    nextSceneId = FOREST_ENCOUNTER_GNURR_DEFEATED_ID;
                    session.setGameOutcomeMessage("Your " + player.getCharacterClass().getPrimaryAttackSkillName()
                            + " connects! The creature is vanquished!");
                }
            } else if (choiceId.equals("FLEE")) {
                nextSceneId = FOREST_ENCOUNTER_FLED_ID;
            } else {
                throw new IllegalArgumentException("Invalid action in battle: " + choiceId);
            }
        } else if (currentSceneId.equals(FOREST_ENCOUNTER_GNURR_DEFEATED_ID)) {
            if (choiceId.equals("TAKE_KEY")) {
                player.setHasAncientKey(true);
                session.setGameOutcomeMessage("You pocket the strange iron key.");
                nextSceneId = KEY_TAKEN_SCENE_ID;

            } else if (choiceId.equals("LEAVE_KEY")) {
                player.setHasAncientKey(false); // Explicitly set to false
                session.setGameOutcomeMessage("You decide to leave the mysterious key behind.");
                nextSceneId = KEY_LEFT_SCENE_ID;

            } else {
                throw new IllegalArgumentException("Invalid choice for key: " + choiceId);
            }
            playerRepository.saveAndFlush(player); // Save player's key status

        } else {
            // Standard choice processing for non-battle, non-special scenes
            Choice chosen = currentScene.getChoices().stream()
                    .filter(c -> c.getId().equals(choiceId))
                    .findFirst()
                    .orElseThrow(() -> {
                        return new IllegalArgumentException(
                                "Invalid choice ID: " + choiceId + " for scene " + currentScene.getId());
                    });
            nextSceneId = chosen.getNextSceneId();
        }

        if (nextSceneId == null) {
            throw new IllegalStateException("Next scene could not be determined.");
        }
        session.setCurrentSceneId(nextSceneId);

        Scene nextSceneDetails = storyScenes.get(nextSceneId);
        if (nextSceneDetails == null) {
            throw new IllegalStateException("Next scene details not found: " + nextSceneId);
        }

        if (nextSceneDetails.isEndingScene()) {
            session.setGameOver(true);
            String finalMessage = nextSceneDetails.getDescription(); // Default ending message
            if (nextSceneId.equals(GAME_OVER_RETURNED_ID) && player.isHasAncientKey()) {
                finalMessage = "You step through the portal and find yourself back in your dorm, the mysterious website now showing a '404 Not Found' error. Was it all a dream? You instinctively reach into your pocket... and feel the cold, hard shape of the ancient iron key. What could this possibly unlock? GAME OVER.";
            }
            session.setGameOutcomeMessage(finalMessage);
        }

        return gameSessionRepository.saveAndFlush(session);
    }

    public Scene getSceneDetails(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Game session not found for scene details: " + sessionId));
        Player player = session.getPlayer();
        String currentStorySceneId = session.getCurrentSceneId();
        Scene originalScene = storyScenes.get(currentStorySceneId);

        if (originalScene == null) {
            throw new IllegalArgumentException("Scene not found in story map: " + currentStorySceneId);
        }

        // Create a new Scene object for display to avoid modifying the original in the
        // map
        Scene sceneForDisplay = new Scene(originalScene.getId(), originalScene.getDescription());
        sceneForDisplay.setBattleScene(originalScene.isBattleScene());
        sceneForDisplay.setEndingScene(originalScene.isEndingScene());

        if (originalScene.isBattleScene() && player.getCharacterClass() != null && !session.isGameOver()) {
            // Only add battle choices if the game is not over

            // Add Punch
            sceneForDisplay.addChoice(new Choice(
                    SKILL_PUNCH.getName().replaceAll("\\s+", "_"),
                    "Use " + SKILL_PUNCH.getName(),
                    null, // Next scene determined by processChoice based on current battle state
                    SKILL_PUNCH.getName(),
                    true));

            // Add Special Attack if character class and skill exist
            CharacterClass charClass = player.getCharacterClass();
            if (charClass != null) {
                String specialSkillName = charClass.getPrimaryAttackSkillName();
                if (specialSkillName != null) {
                    // Check if player actually knows this skill (they should if it's their primary)
                    boolean knowsSpecial = player.getKnownSkills().stream()
                            .anyMatch(s -> s.getName().equals(specialSkillName));
                    if (knowsSpecial) {
                        sceneForDisplay.addChoice(new Choice(
                                specialSkillName.replaceAll("\\s+", "_"),
                                "Use " + specialSkillName,
                                null, // Next scene determined by processChoice
                                specialSkillName,
                                true));
                    }
                }
            }

            sceneForDisplay.addChoice(new Choice("FLEE", "Try to Flee", null, null, true));

        } else if (!originalScene.isEndingScene()) { // Only add predefined choices if not an ending scene (unless
                                                     // ending has choices)
            // For non-battle scenes, or if battle scene has predefined choices (though we
            // build dynamically now)
            // Ensure originalScene.getChoices() is not null
            if (originalScene.getChoices() != null) {
                sceneForDisplay.setChoices(new ArrayList<>(originalScene.getChoices()));
            } else {
                sceneForDisplay.setChoices(new ArrayList<>()); // Ensure choices list is not null
            }
        }
        // If it's an ending scene, it might have no choices, or its predefined choices
        // are fine (like "Play Again?")
        // but our current ending scenes don't have choices that lead to further game
        // play.

        return sceneForDisplay;
    }

    public Optional<GameSession> getGameSession(Long sessionId) {
        return gameSessionRepository.findById(sessionId);
    }
}
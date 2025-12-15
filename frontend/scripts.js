document.addEventListener('DOMContentLoaded', () => {
    const sceneDescriptionElement = document.getElementById('sceneDescription');
    const choicesSectionElement = document.getElementById('choices-section');
    const playerNameInput = document.getElementById('playerNameInput');
    const startGameButton = document.getElementById('startGameButton');
    const inputSectionElement = document.getElementById('input-section');
    const gameOverSectionElement = document.getElementById('game-over-section');
    const outcomeMessageElement = document.getElementById('outcomeMessage');
    const restartGameButton = document.getElementById('restartGameButton');
    const playerNameDisplay = document.getElementById('playerName');
    const playerClassDisplay = document.getElementById('playerClass');
    const playerInfoSection = document.getElementById('player-info-section');
    const exitGameButton = document.getElementById('exitGameButton');
    const gameControlsElement = document.getElementById('game-controls');
    const attackEffectMessageElement = document.getElementById('attack-effect-message');


    let API_BASE_URL;
    const currentHostname = window.location.hostname;
    const currentProtocol = window.location.protocol;

    // Check for local development scenarios
    if (currentProtocol === "file:" || currentHostname === "localhost" || currentHostname === "127.0.0.1") {
        API_BASE_URL = 'http://localhost:8080/api/game';
    } else {
        API_BASE_URL = 'https://byu-student-java-text-rpg.onrender.com/api/game';
    }

    let currentSessionId = null;
    let currentPlayerName = '';

    playerInfoSection.style.display = 'none'; // Hiding player info initially

    // Function to update the game display on each selection (SPA feature)
    function updateDisplay(gameState) {
        choicesSectionElement.innerHTML = '';
        const storySectionElement = document.getElementById('story-section');
        const sceneImageElement = document.getElementById('sceneImage');
        const sceneImageContainer = document.getElementById('scene-image-container');

        // Stats Elements
        const statRigging = document.getElementById('statRigging');
        const statLogic = document.getElementById('statLogic');
        const statNerve = document.getElementById('statNerve');

        if (gameState.gameOver) {
            if (gameControlsElement) gameControlsElement.style.display = 'none';
            sceneDescriptionElement.textContent = '';
            storySectionElement.style.display = 'none';
            if (sceneImageContainer) sceneImageContainer.style.display = 'none'; // Hide image on game over

            inputSectionElement.style.display = 'none';
            choicesSectionElement.style.display = 'none';

            outcomeMessageElement.textContent = gameState.outcomeMessage;

            gameOverSectionElement.style.display = 'block';
            playerInfoSection.style.display = 'none';
        } else {
            if (gameControlsElement) gameControlsElement.style.display = 'block';

            if (gameState.imageUrl) {
                sceneImageElement.src = gameState.imageUrl;
                sceneImageContainer.style.display = 'block';
            } else {
                sceneImageContainer.style.display = 'none';
            }

            storySectionElement.style.display = 'block';
            sceneDescriptionElement.innerText = gameState.description;

            // Handle Choices
            gameState.choices.forEach(choice => {
                const button = document.createElement('button');
                button.setAttribute('type', 'button');
                button.textContent = choice.text;
                button.classList.add('choice-button');
                button.addEventListener('click', (event) => {
                    makeChoice(choice.id);
                });
                choicesSectionElement.appendChild(button);
            });
            choicesSectionElement.style.display = 'block';
            gameOverSectionElement.style.display = 'none';
            inputSectionElement.style.display = 'none';

            // Handle Stats
            if (gameState.playerName) {
                playerNameDisplay.textContent = gameState.playerName;
                // Update new stats
                if(statRigging) statRigging.textContent = gameState.rigging;
                if(statLogic) statLogic.textContent = gameState.logic;
                if(statNerve) statNerve.textContent = gameState.nerve;

                playerInfoSection.style.display = 'block';
            }
        }
    }

    const loadingIndicatorElement = document.getElementById('loading-indicator');

    function showLoading() {
        if (loadingIndicatorElement) loadingIndicatorElement.style.display = 'block';
    }

    function hideLoading() {
        if (loadingIndicatorElement) loadingIndicatorElement.style.display = 'none';
    }

    // Function to start the game
    async function startGame() {
        currentPlayerName = playerNameInput.value.trim();
        showLoading();
        startGameButton.disabled = true;
        if (!currentPlayerName) {
            alert('Please enter your name!');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/start`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ playerName: currentPlayerName }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
            }

            const gameState = await response.json();
            currentSessionId = gameState.sessionId;
            updateDisplay(gameState);
            playerNameInput.value = ''; // Clear input
            inputSectionElement.style.display = 'none';

            if (gameControlsElement) gameControlsElement.style.display = 'block';

        } catch (error) {
            console.error('Error starting game:', error);
            sceneDescriptionElement.textContent = `Error starting game: ${error.message}. Check console.`;
        } finally {
            hideLoading();
            startGameButton.disabled = false;
        }
    }

    // Function to make a choice
    async function makeChoice(choiceId) {
        if (!currentSessionId) {
            console.error('No active game session!');
            sceneDescriptionElement.textContent = "Session lost or not started. Please start a new game.";
            inputSectionElement.style.display = 'block';
            choicesSectionElement.innerHTML = '';
            if (gameControlsElement) gameControlsElement.style.display = 'none';
            playerInfoSection.style.display = 'none';
            return;
        }

        let isAttackAction = false;
        let attackMessage = "";

        const knownAttackChoiceIdPatterns = ["Punch", "Power_Slash", "Ranged_Arrow"];
        if (knownAttackChoiceIdPatterns.some(pattern => choiceId.toLowerCase().includes(pattern.toLowerCase()))) {
            isAttackAction = true;
            if (choiceId.toLowerCase().includes("punch")) attackMessage = "PUNCH!";
            else if (choiceId.toLowerCase().includes("slash")) attackMessage = "POWER SLASH!";
            else if (choiceId.toLowerCase().includes("arrow")) attackMessage = "RANGED ARROW!";
            else attackMessage = "ATTACK!";
        }

        document.querySelectorAll('.choice-button').forEach(btn => btn.disabled = true);
        choicesSectionElement.innerHTML = '';

        if (isAttackAction && attackEffectMessageElement) {
            attackEffectMessageElement.textContent = attackMessage;
            attackEffectMessageElement.style.display = 'block';

            // Wait a short moment for the player to see the attack message
            await new Promise(resolve => setTimeout(resolve, 1500)); // 1.5-second delay

            // Hide the attack message after the delay, before fetching next state
            attackEffectMessageElement.style.display = 'none';
            attackEffectMessageElement.textContent = '';
        }

        try {
            const response = await fetch(`${API_BASE_URL}/${currentSessionId}/action`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ choiceId: choiceId }),
            });

            let responseBodyText = await response.text();

            if (!response.ok) {
                let errorMessage = `HTTP error! Status: ${response.status}`;
                try {
                    const errorData = JSON.parse(responseBodyText);
                    errorMessage = errorData.error || errorData.message || (errorData.detail || `Server error: ${response.status}`);
                } catch (e) {

                    if (responseBodyText && responseBodyText.trim() !== "") {
                        errorMessage = responseBodyText;
                    }
                }
                console.error("Full error response object from makeChoice API:", response);
                throw new Error(errorMessage);
            }

            const gameState = JSON.parse(responseBodyText);
            updateDisplay(gameState);

        } catch (error) {
            console.error('Error in makeChoice JS function:', error);
            sceneDescriptionElement.textContent = `Error processing your choice: ${error.message}. Please try refreshing or starting a new game if the issue persists.`;
        } finally {
            hideLoading();
            // Buttons are re-created by updateDisplay
        }
    }

    if (gameControlsElement) gameControlsElement.style.display = 'none';

    // Event Listeners
    exitGameButton.addEventListener('click', () => {
        if (confirm("Are you sure you want to exit? Your current progress will be lost.")) {
            currentSessionId = null;
            gameOverSectionElement.style.display = 'none';
            inputSectionElement.style.display = 'block';
            choicesSectionElement.innerHTML = '';
            choicesSectionElement.style.display = 'block';
            sceneDescriptionElement.textContent = 'Welcome, adventurer! Enter your name to begin.';
            playerNameInput.value = '';
            playerNameDisplay.textContent = '';
            playerClassDisplay.textContent = '';
            playerInfoSection.style.display = 'none';
            if (gameControlsElement) gameControlsElement.style.display = 'none';
        }
    });

    startGameButton.addEventListener('click', startGame);
    restartGameButton.addEventListener('click', () => {
        // Reset UI for new game
        currentSessionId = null;
        gameOverSectionElement.style.display = 'none';
        inputSectionElement.style.display = 'block';
        choicesSectionElement.style.display = 'block';
        sceneDescriptionElement.textContent = 'Welcome, adventurer! Enter your name to begin.';
        playerNameDisplay.textContent = '';
        playerClassDisplay.textContent = '';
        playerInfoSection.style.display = 'none';

        if (gameControlsElement) gameControlsElement.style.display = 'none';
    });

    playerNameInput.addEventListener('keypress', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            startGameButton.click();
        }
    });
});
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

    // Function to update the game display on each selection like a SPA
    function updateDisplay(gameState) {

        choicesSectionElement.innerHTML = '';
        const storySectionElement = document.getElementById('story-section');

        if (gameState.gameOver) {
            if (gameControlsElement) gameControlsElement.style.display = 'none';

            sceneDescriptionElement.textContent = '';
            storySectionElement.style.display = 'none';

            inputSectionElement.style.display = 'none';
            choicesSectionElement.style.display = 'none';

            // Only use outcomeMessage for the game over section
            outcomeMessageElement.textContent = gameState.outcomeMessage;

            gameOverSectionElement.style.display = 'block';
            playerInfoSection.style.display = 'none';
        } else {
            if (gameControlsElement) gameControlsElement.style.display = 'block';

            storySectionElement.style.display = 'block'; // Show the main story section
            sceneDescriptionElement.textContent = gameState.description;

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

            if (gameState.playerName) {
                playerNameDisplay.textContent = gameState.playerName;
                playerClassDisplay.textContent = gameState.playerClass || 'Undetermined';
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
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/${currentSessionId}/action`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ choiceId: choiceId }),
            });

            // EXPANDED error response as I couldn't seem to figure out why it was
            // failing every time I selected a weapon in the list. Source it easier
            if (!response.ok) {
                let errorMessage = `HTTP error! status: ${response.status}`;
                try {
                    const errorData = await response.json();
                    errorMessage = errorData.error || errorData.message || errorMessage;
                } catch (e) {
                    const errorText = await response.text();
                    errorMessage = errorText || errorMessage;
                }
                throw new Error(errorMessage);
            }

            const gameState = await response.json();
            updateDisplay(gameState);
        } catch (error) {
            console.error('Error making choice:', error);
            sceneDescriptionElement.textContent = `Error: ${error.message}. Check console.`;
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
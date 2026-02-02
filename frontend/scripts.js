document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const sceneDescriptionElement = document.getElementById('sceneDescription');
    const choicesSectionElement = document.getElementById('choices-section');
    const playerNameInput = document.getElementById('playerNameInput');
    const startGameButton = document.getElementById('startGameButton');
    const inputSectionElement = document.getElementById('input-section');
    const gameOverSectionElement = document.getElementById('game-over-section');
    const outcomeMessageElement = document.getElementById('outcomeMessage');
    const restartGameButton = document.getElementById('restartGameButton');
    const playerNameDisplay = document.getElementById('playerName');
    // Stats
    const statRigging = document.getElementById('statRigging');
    const statLogic = document.getElementById('statLogic');
    const statNerve = document.getElementById('statNerve');
    const playerInfoSection = document.getElementById('player-info-section');
    const gameControlsElement = document.getElementById('game-controls');
    const sceneImageElement = document.getElementById('sceneImage');
    const sceneImageContainer = document.getElementById('scene-image-container');
    const loadingIndicatorElement = document.getElementById('loading-indicator');

    // Environment Configuration
    let API_BASE_URL;
    const currentHostname = window.location.hostname;
    const currentProtocol = window.location.protocol;

    if (currentProtocol === "file:" || currentHostname === "localhost" || currentHostname === "127.0.0.1") {
        API_BASE_URL = 'http://localhost:8080/api/game';
        console.log("Running in LOCAL mode.");
    } else {
        API_BASE_URL = 'https://adventure.aarontopping.com/api/game';
        console.log("Running in DEPLOYED mode.");
    }

    let currentSessionId = null;
    let currentPlayerName = '';

    // --- UTILITY FUNCTIONS ---

    function showLoading() {
        if (loadingIndicatorElement) loadingIndicatorElement.style.display = 'block';
    }

    function hideLoading() {
        if (loadingIndicatorElement) loadingIndicatorElement.style.display = 'none';
    }

    // Helper for delays
    const sleep = (ms) => new Promise(r => setTimeout(r, ms));

    // Special Animation for the Boot Sequence
    async function playBootSequence(text, onComplete) {
        sceneDescriptionElement.innerHTML = ""; // Clear
        choicesSectionElement.style.display = 'none'; // Hide buttons during animation

        const lines = text.split('\n');

        for (let line of lines) {
            // Create a container for the line
            const lineContainer = document.createElement('div');
            lineContainer.style.marginBottom = "5px";
            lineContainer.style.fontFamily = "'Courier New', monospace";
            sceneDescriptionElement.appendChild(lineContainer);

            // Check for the "Loading... OK" pattern
            if (line.includes("... OK")) {
                const parts = line.split("..."); // ["Loading Kernel", " OK."]
                const mainText = parts[0];
                const statusText = parts[1];

                // Type main part
                lineContainer.textContent = mainText;
                await sleep(200); 
                
                // Animate dots
                for(let i=0; i<3; i++) {
                    lineContainer.textContent += ".";
                    await sleep(400); // 400ms delay between dots
                }

                // Append status (Green OK)
                const statusSpan = document.createElement('span');
                statusSpan.textContent = statusText;
                statusSpan.style.color = "#00ff00";
                statusSpan.style.fontWeight = "bold";
                lineContainer.appendChild(statusSpan);
                
                await sleep(300);
            } else {
                // Normal text line, just print it
                lineContainer.textContent = line;
                await sleep(100);
            }
        }
        
        onComplete();
    }

    // --- MAIN DISPLAY LOGIC ---

    function updateDisplay(gameState) {
        // Reset UI Components
        choicesSectionElement.innerHTML = '';
        const storySectionElement = document.getElementById('story-section');
        
        // Handle Game Over
        if (gameState.gameOver) {
            if (gameControlsElement) gameControlsElement.style.display = 'none';
            sceneDescriptionElement.textContent = '';
            storySectionElement.style.display = 'none';
            if(sceneImageContainer) sceneImageContainer.style.display = 'none';
            inputSectionElement.style.display = 'none';
            choicesSectionElement.style.display = 'none';
            
            outcomeMessageElement.innerText = gameState.outcomeMessage; // innerText handles newlines
            gameOverSectionElement.style.display = 'block';
            playerInfoSection.style.display = 'none';
            return; 
        }

        // Handle Active Game
        if (gameControlsElement) gameControlsElement.style.display = 'block';
        storySectionElement.style.display = 'block';
        gameOverSectionElement.style.display = 'none';
        inputSectionElement.style.display = 'none';

        // 3. Handle Image
        if (gameState.imageUrl) {
            sceneImageElement.src = gameState.imageUrl;
            sceneImageContainer.style.display = 'block';
        } else {
            sceneImageContainer.style.display = 'none';
        }

        // Handle Text & Animation
        // If it's the specific "Login" scene, run the boot animation
        if (gameState.description.includes("BOOT SEQUENCE")) {
            playBootSequence(gameState.description, () => {
                renderChoices(gameState.choices);
            });
        } else {
            // Normal display
            sceneDescriptionElement.innerText = gameState.description;
            renderChoices(gameState.choices);
        }

        // Update Stats
        if (gameState.playerName) {
            playerNameDisplay.textContent = gameState.playerName;
            if(statRigging) statRigging.textContent = gameState.rigging;
            if(statLogic) statLogic.textContent = gameState.logic;
            if(statNerve) statNerve.textContent = gameState.nerve;
            playerInfoSection.style.display = 'block';
        }
    }

    function renderChoices(choices) {
        choicesSectionElement.innerHTML = ''; // Clear
        choicesSectionElement.style.display = 'block';
        
        choices.forEach(choice => {
            const button = document.createElement('button');
            button.setAttribute('type', 'button');
            button.textContent = choice.text;
            button.classList.add('choice-button');
            button.addEventListener('click', (event) => {
                makeChoice(choice.id);
            });
            choicesSectionElement.appendChild(button);
        });
    }

    // --- API INTERACTION ---

    async function startGame() {
        currentPlayerName = playerNameInput.value.trim();
        if (!currentPlayerName) {
            alert('Please enter your name!');
            return;
        }

        showLoading();
        startGameButton.disabled = true;

        try {
            const response = await fetch(`${API_BASE_URL}/start`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ playerName: currentPlayerName }),
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            const gameState = await response.json();
            currentSessionId = gameState.sessionId;
            updateDisplay(gameState);
            
            playerNameInput.value = '';
            inputSectionElement.style.display = 'none';
        } catch (error) {
            console.error('Error starting game:', error);
            sceneDescriptionElement.textContent = `Error: ${error.message}`;
        } finally {
            hideLoading();
            startGameButton.disabled = false;
        }
    }

    async function makeChoice(choiceId) {
        if (!currentSessionId) return;

        showLoading();
        // Disable buttons
        const buttons = document.querySelectorAll('.choice-button');
        buttons.forEach(b => b.disabled = true);

        try {
            const response = await fetch(`${API_BASE_URL}/${currentSessionId}/action`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ choiceId: choiceId }),
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

            const gameState = await response.json();
            updateDisplay(gameState);
        } catch (error) {
            console.error('Error:', error);
            sceneDescriptionElement.textContent = `Error: ${error.message}`;
        } finally {
            hideLoading();
        }
    }

    // --- EVENT LISTENERS ---
    
    startGameButton.addEventListener('click', startGame);
    
    if (restartGameButton) {
        restartGameButton.addEventListener('click', () => {
            currentSessionId = null;
            gameOverSectionElement.style.display = 'none';
            inputSectionElement.style.display = 'block';
            choicesSectionElement.innerHTML = '';
            sceneDescriptionElement.textContent = 'System Ready. Enter Subject Name.';
            if(sceneImageContainer) sceneImageContainer.style.display = 'none';
            if (gameControlsElement) gameControlsElement.style.display = 'none';
            playerInfoSection.style.display = 'none';
        });
    }

    if (playerNameInput) {
        playerNameInput.addEventListener('keypress', function (event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                startGameButton.click();
            }
        });
    }
    
    // Initial UI Setup
    if (gameControlsElement) gameControlsElement.style.display = 'none';
    const exitBtn = document.getElementById('exitGameButton');
    if(exitBtn) {
        exitBtn.addEventListener('click', () => {
            if(confirm("Terminate Session? Progress will be lost.")) {
                location.reload(); // Simple reload to reset
            }
        });
    }
});
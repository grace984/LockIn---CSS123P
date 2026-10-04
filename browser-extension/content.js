let restrictedSites = [];
let warningShown = false;
let warningOverlay = null;
let currentSound = 2;

async function loadRestrictedSites() {
    try {
        const response = await chrome.runtime.sendMessage({
            action: "getRestrictedSites"
        });

        if (Array.isArray(response)) {
            restrictedSites = response;
        }
    } catch (error) {
        console.log("LockIn restricted sites error:", error);
    }
}

async function loadSelectedSound() {
    try {
        const response = await chrome.runtime.sendMessage({
            action: "getSelectedSound"
        });

        if (response === 1 || response === 2) {
            currentSound = response;
        } else {
            currentSound = 2;
        }
    } catch (error) {
        console.log("LockIn sound error:", error);
        currentSound = 2;
    }
}

async function getTimerStatus() {
    try {
        const response = await chrome.runtime.sendMessage({
            action: "getTimerStatus"
        });

        return response || {
            active: false,
            mode: "unknown"
        };
    } catch (error) {
        console.log("LockIn timer error:", error);

        return {
            active: false,
            mode: "unknown"
        };
    }
}

async function getWarningMessage() {
    try {
        const response = await chrome.runtime.sendMessage({
            action: "getWarningMessage"
        });

        return response ||
            "This website is on your distraction list.";
    } catch (error) {
        console.log("LockIn warning message error:", error);

        return "This website is on your distraction list.";
    }
}


// ========================================
// BEEP SOUND
// ========================================

function playBeep() {
    try {
        const AudioContext =
            window.AudioContext ||
            window.webkitAudioContext;

        const audioContext = new AudioContext();

        const oscillator =
            audioContext.createOscillator();

        const gainNode =
            audioContext.createGain();

        // Electronic beep
        oscillator.type = "square";

        oscillator.frequency.setValueAtTime(
            1000,
            audioContext.currentTime
        );

        // BEEP VOLUME / GAIN
        gainNode.gain.setValueAtTime(
            0.45,
            audioContext.currentTime
        );

        // Short beep
        gainNode.gain.exponentialRampToValueAtTime(
            0.01,
            audioContext.currentTime + 0.18
        );

        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);

        oscillator.start();

        oscillator.stop(
            audioContext.currentTime + 0.18
        );

    } catch (error) {
        console.log("LockIn beep error:", error);
    }
}


// ========================================
// PLAY SELECTED WARNING SOUND
// ========================================

function playWarningSound() {

    // Sound 1 = one BEEP
    if (currentSound === 1) {
        playBeep();
        return;
    }

    // Sound 2 = BEEP BEEP
    playBeep();

    setTimeout(() => {
        playBeep();
    }, 250);
}


// ========================================
// WARNING OVERLAY
// ========================================

function showWarning(message) {

    if (warningShown) {
        return;
    }

    warningShown = true;

    playWarningSound();

    warningOverlay =
        document.createElement("div");

    warningOverlay.id =
        "lockin-warning-overlay";

    warningOverlay.style.position = "fixed";
    warningOverlay.style.top = "0";
    warningOverlay.style.left = "0";
    warningOverlay.style.width = "100%";
    warningOverlay.style.height = "100%";

    warningOverlay.style.background =
        "rgba(20, 20, 30, 0.96)";

    warningOverlay.style.zIndex =
        "2147483647";

    warningOverlay.style.display =
        "flex";

    warningOverlay.style.flexDirection =
        "column";

    warningOverlay.style.justifyContent =
        "center";

    warningOverlay.style.alignItems =
        "center";

    warningOverlay.style.textAlign =
        "center";

    warningOverlay.style.fontFamily =
        "Arial, sans-serif";

    warningOverlay.innerHTML = `
        <div style="
            max-width: 600px;
            padding: 40px;
        ">
            <div style="
                font-size: 64px;
                margin-bottom: 20px;
            ">
                ⚠️
            </div>

            <h1 style="
                color: white;
                font-size: 36px;
                margin-bottom: 20px;
            ">
                Stay Focused!
            </h1>

            <p style="
                color: #dddddd;
                font-size: 20px;
                line-height: 1.5;
            ">
                ${escapeHtml(message)}
            </p>

            <p style="
                color: #aaaaaa;
                font-size: 15px;
                margin-top: 25px;
            ">
                This website is restricted during your Work session.
            </p>
        </div>
    `;

    document.body.appendChild(
        warningOverlay
    );
}


// ========================================
// REMOVE WARNING
// ========================================

function removeWarning() {

    if (warningOverlay) {
        warningOverlay.remove();
        warningOverlay = null;
    }

    warningShown = false;
}


// ========================================
// CHECK CURRENT WEBSITE
// ========================================

async function checkWebsite() {

    await loadRestrictedSites();
    await loadSelectedSound();

    const timerStatus =
        await getTimerStatus();

    // Only warn during active Work timer
    if (
        !timerStatus.active ||
        timerStatus.mode !== "work"
    ) {
        removeWarning();
        return;
    }

    const hostname =
        window.location.hostname
            .replace(/^www\./, "")
            .toLowerCase();

    const isRestricted =
        restrictedSites.some(site => {

            const cleanSite =
                String(site)
                    .replace(/^www\./, "")
                    .toLowerCase()
                    .trim();

            return (
                hostname === cleanSite ||
                hostname.endsWith("." + cleanSite)
            );
        });

    if (isRestricted) {

        const message =
            await getWarningMessage();

        showWarning(message);

    } else {

        removeWarning();
    }
}


// ========================================
// ESCAPE HTML
// ========================================

function escapeHtml(text) {

    const div =
        document.createElement("div");

    div.textContent = text;

    return div.innerHTML;
}


// ========================================
// START
// ========================================

checkWebsite();


// Check again periodically
setInterval(() => {
    checkWebsite();
}, 2000);
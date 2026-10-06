// Stores the list of restricted websites received from the Java app.
let restrictedSites = [];

// True while the warning screen is showing so we don't show it twice.
let warningShown = false;

// Holds the warning screen element so we can remove it later.
let warningOverlay = null;

// The warning sound currently selected (1 or 2).
let currentSound = 2;


// LOAD RESTRICTED SITES

// Asks background.js for the restricted sites list and saves it.
async function loadRestrictedSites() {
    try {
        const response = await chrome.runtime.sendMessage({
            action: "getRestrictedSites"
        });

        // Only save the reply if it is really a list.
        if (Array.isArray(response)) {
            restrictedSites = response;
        }
    } catch (error) {
        console.log(
            "LockIn restricted sites error:",
            error
        );
    }
}


// LOAD SELECTED SOUND

// Asks background.js which warning sound the user picked and saves it.
async function loadSelectedSound() {
    try {
        const response = await chrome.runtime.sendMessage({
            action: "getSelectedSound"
        });

        // Accept only sound 1 or 2, otherwise use sound 2.
        if (response === 1 || response === 2) {
            currentSound = response;
        } else {
            currentSound = 2;
        }

        console.log(
            "LockIn selected sound:",
            currentSound
        );

    } catch (error) {

        console.log(
            "LockIn sound error:",
            error
        );

        currentSound = 2;
    }
}


// GET TIMER STATUS

// Asks background.js if the timer is active and which mode it is in.
async function getTimerStatus() {
    try {

        const response =
            await chrome.runtime.sendMessage({
                action: "getTimerStatus"
            });

        // Use a "not active" default if there is no reply.
        return response || {
            active: false,
            mode: "unknown"
        };

    } catch (error) {

        console.log(
            "LockIn timer error:",
            error
        );

        return {
            active: false,
            mode: "unknown"
        };
    }
}


// GET WARNING MESSAGE

// Asks background.js for the warning message to show on the screen.
async function getWarningMessage() {
    try {

        const response =
            await chrome.runtime.sendMessage({
                action: "getWarningMessage"
            });

        // Use the default message if there is no reply.
        return response ||
            "This website is on your distraction list.";

    } catch (error) {

        console.log(
            "LockIn warning message error:",
            error
        );

        return "This website is on your distraction list.";
    }
}


// PLAY WARNING SOUND

// Asks background.js to play the currently selected warning sound.
async function playWarningSound() {

    console.log(
        "LockIn requesting warning sound:",
        currentSound
    );

    try {

        await chrome.runtime.sendMessage({
            action: "playWarningSound",
            sound: currentSound
        });

    } catch (error) {

        console.log(
            "LockIn warning sound request error:",
            error
        );
    }
}


// WARNING OVERLAY

// Covers the whole page with a dark warning screen and plays the warning sound.
function showWarning(message) {

    // Do nothing if the warning is already showing.
    if (warningShown) {
        return;
    }

    warningShown = true;

    // Play sound when warning first appears
    playWarningSound();

    // Create the full-screen warning container.
    warningOverlay =
        document.createElement("div");

    warningOverlay.id =
        "lockin-warning-overlay";

    // Style the container so it covers the whole page and sits on top of everything.
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


    // Fill the container with the warning icon, the message, and the explanation text.
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
                ${escapeHtml(message)}
            </h1>

            <p style="
                color: #aaaaaa;
                font-size: 20px;
                line-height: 1.5;
                margin-top: 0;
            ">
                This website is restricted during your Work session.
            </p>

        </div>
    `;


    // Put the warning screen on the page.
    document.body.appendChild(
        warningOverlay
    );
}


// REMOVE WARNING

// Takes the warning screen off the page and resets the flag.
function removeWarning() {

    if (warningOverlay) {

        warningOverlay.remove();

        warningOverlay = null;
    }

    warningShown = false;
}


// CHECK CURRENT WEBSITE

// Checks if the current site is restricted during a Work session and shows or removes the warning.
async function checkWebsite() {

    // Get the newest restricted sites list.
    await loadRestrictedSites();

    // Get the newest selected sound.
    await loadSelectedSound();


    // Find out if the timer is running.
    const timerStatus =
        await getTimerStatus();


    // Remove the warning if the timer is off or not in Work mode.
    if (
        !timerStatus.active ||
        timerStatus.mode !== "work"
    ) {

        removeWarning();

        return;
    }


    // Get the current site's domain in a clean lowercase form.
    const hostname =
        window.location.hostname
            .replace(/^www\./, "")
            .toLowerCase();


    // Check if the current domain (or its subdomain) is in the restricted list.
    const isRestricted =
        restrictedSites.some(
            function (site) {

                // Clean the saved site the same way as the hostname.
                const cleanSite =
                    String(site)
                        .replace(/^www\./, "")
                        .toLowerCase()
                        .trim();


                return (
                    hostname === cleanSite ||
                    hostname.endsWith(
                        "." + cleanSite
                    )
                );
            }
        );


    // Show the warning for restricted sites, otherwise remove it.
    if (isRestricted) {

        const message =
            await getWarningMessage();

        showWarning(message);

    } else {

        removeWarning();
    }
}


// ESCAPE HTML

// Turns special characters in text into safe text so it can't inject HTML into the page.
function escapeHtml(text) {

    const div =
        document.createElement("div");

    div.textContent = text;

    return div.innerHTML;
}


// START

// Run the check once right when the page loads.
checkWebsite();


// Keep re-checking every 2 seconds so changes in the timer or list are picked up.
setInterval(
    function () {

        checkWebsite();

    },
    2000
);
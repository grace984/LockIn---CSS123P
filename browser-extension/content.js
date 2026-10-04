const DEFAULT_MESSAGE = "This website is on your distraction list.";
const CHECK_INTERVAL_MS = 2000;

let checkTimer = null;


/* ---------------------------------------------------------
   SAFE MESSAGING
   If the extension is reloaded while a page is open, the old
   content script can no longer talk to it. This helper stops
   the polling instead of throwing errors forever.
--------------------------------------------------------- */
function askBackground(message, callback) {

    try {

        chrome.runtime.sendMessage(message, function (reply) {

            if (chrome.runtime.lastError) {
                console.log(
                    "LockIn error (" + message.action + "):",
                    chrome.runtime.lastError.message
                );
                callback(undefined);
                return;
            }

            callback(reply);
        });

    } catch (error) {

        console.log("LockIn: extension reloaded, stopping checks.");

        if (checkTimer) {
            clearInterval(checkTimer);
        }
    }
}


/* ---------------------------------------------------------
   STEP 1: IS THE WORK TIMER RUNNING?
--------------------------------------------------------- */
function checkLockInStatus() {

    askBackground({ action: "getTimerStatus" }, function (timerStatus) {

        if (
            !timerStatus ||
            timerStatus.active !== true ||
            timerStatus.mode !== "work"
        ) {
            removeWarning();
            return;
        }

        checkRestrictedSite();
    });
}


/* ---------------------------------------------------------
   STEP 2: IS THIS SITE ON THE DISTRACTION LIST?
--------------------------------------------------------- */
function checkRestrictedSite() {

    askBackground({ action: "getRestrictedSites" }, function (restrictedSites) {

        if (!Array.isArray(restrictedSites)) {
            return;
        }

        const currentHostname =
            window.location.hostname
                .toLowerCase()
                .replace(/^www\./, "");

        if (!currentHostname) {
            return;
        }

        const isRestricted = restrictedSites.some(function (site) {

            const cleanSite =
                String(site)
                    .toLowerCase()
                    .replace(/^www\./, "");

            return (
                currentHostname === cleanSite ||
                currentHostname.endsWith("." + cleanSite)
            );
        });

        if (!isRestricted) {
            removeWarning();
            return;
        }

        showWarning(currentHostname);
    });
}


/* ---------------------------------------------------------
   STEP 3: SHOW THE WARNING (OR REFRESH IT IF ALREADY SHOWN)
--------------------------------------------------------- */
function showWarning(currentHostname) {

    // Warning is already on screen: only refresh the message text,
    // so a newly selected Version 1/2/3 shows up without a reload.
    if (document.getElementById("lockin-warning")) {
        updateWarningMessage();
        return;
    }

    const overlay = document.createElement("div");
    overlay.id = "lockin-warning";

    overlay.innerHTML = `
        <div id="lockin-warning-box">
            <div class="lockin-title">LockIn Warning</div>
            <div class="lockin-message"></div>
            <div class="lockin-site"></div>
            <button id="lockin-close">Continue</button>
        </div>
    `;

    // Set as text (not innerHTML) so nothing in the values can inject HTML.
    overlay.querySelector(".lockin-message").textContent = DEFAULT_MESSAGE;
    overlay.querySelector(".lockin-site").textContent = currentHostname;

    const style = document.createElement("style");
    style.id = "lockin-warning-style";

    style.textContent = `
        #lockin-warning {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.65);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 2147483647;
            font-family: Arial, sans-serif;
        }

        #lockin-warning-box {
            background: white;
            color: black;
            width: 360px;
            padding: 30px;
            border-radius: 15px;
            text-align: center;
            box-shadow: 0 8px 30px rgba(0, 0, 0, 0.35);
        }

        .lockin-title {
            font-size: 26px;
            font-weight: bold;
            margin-bottom: 15px;
        }

        .lockin-message {
            font-size: 16px;
            margin-bottom: 12px;
        }

        .lockin-site {
            font-size: 15px;
            font-weight: bold;
            margin-bottom: 25px;
            word-break: break-word;
        }

        #lockin-close {
            border: none;
            padding: 10px 25px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 15px;
        }

        #lockin-close:hover {
            opacity: 0.85;
        }
    `;

    document.head.appendChild(style);
    document.documentElement.appendChild(overlay);

    overlay
        .querySelector("#lockin-close")
        .addEventListener("click", function () {
            removeWarning();
        });

    updateWarningMessage();
}


/* ---------------------------------------------------------
   ASK THE BACKGROUND SCRIPT FOR THE SELECTED MESSAGE
   Expected reply: a plain string, e.g. "Stay focused!"
--------------------------------------------------------- */
function updateWarningMessage() {

    askBackground({ action: "getWarningMessage" }, function (text) {

        console.log("LockIn warning message reply:", text);

        if (typeof text !== "string" || text.trim() === "") {
            return;
        }

        const line = document.querySelector("#lockin-warning .lockin-message");

        if (line && line.textContent !== text) {
            line.textContent = text;
        }
    });
}


/* ---------------------------------------------------------
   REMOVE THE WARNING
--------------------------------------------------------- */
function removeWarning() {

    const overlay = document.getElementById("lockin-warning");

    if (overlay) {
        overlay.remove();
    }

    const style = document.getElementById("lockin-warning-style");

    if (style) {
        style.remove();
    }
}


// CHECK IMMEDIATELY, THEN KEEP CHECKING
checkLockInStatus();
checkTimer = setInterval(checkLockInStatus, CHECK_INTERVAL_MS);
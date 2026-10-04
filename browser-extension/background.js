const OFFSCREEN_DOCUMENT = "offscreen.html";

let creatingOffscreenDocument = null;


// ================================
// TRACK ACTIVE TAB
// ================================

chrome.tabs.onActivated.addListener(async (activeInfo) => {
    try {
        const tab = await chrome.tabs.get(activeInfo.tabId);

        if (tab.url) {
            sendWebsite(tab.url);
        }
    } catch (error) {
        console.log("LockIn tab activation error:", error);
    }
});


// ================================
// TRACK URL CHANGES + PAGE LOAD
// ================================

chrome.tabs.onUpdated.addListener(
    (tabId, changeInfo, tab) => {

        if (
            changeInfo.url &&
            changeInfo.url === tab.url
        ) {
            sendWebsite(changeInfo.url);
        }

        if (
            changeInfo.status === "complete" &&
            tab.url
        ) {
            sendWebsite(tab.url);
        }
    }
);


// ================================
// SEND WEBSITE TO JAVA API
// ================================

function sendWebsite(url) {

    try {

        if (
            !url ||
            url.startsWith("chrome://") ||
            url.startsWith("chrome-extension://") ||
            url.startsWith("edge://") ||
            url.startsWith("about:")
        ) {
            return;
        }

        const website =
            new URL(url).hostname
                .replace(/^www\./, "");

        if (!website) {
            return;
        }

        fetch(
            "http://localhost:8080/website",
            {
                method: "POST",

                headers: {
                    "Content-Type": "text/plain"
                },

                body: website
            }
        )
        .then(response =>
            response.text()
        )
        .then(data => {

            console.log(
                "LockIn website status:",
                website,
                data
            );

        })
        .catch(error => {

            console.log(
                "LockIn API error:",
                error
            );

        });

    } catch (error) {

        console.log(
            "LockIn website tracking error:",
            error
        );

    }
}


// ================================
// OFFSCREEN DOCUMENT
// ================================

async function setupOffscreenDocument() {

    if (chrome.offscreen.hasDocument) {

        const hasDocument =
            await chrome.offscreen.hasDocument();

        if (hasDocument) {
            return;
        }

    } else {

        const existingContexts =
            await chrome.runtime.getContexts({
                contextTypes: [
                    "OFFSCREEN_DOCUMENT"
                ],
                documentUrls: [
                    chrome.runtime.getURL(
                        OFFSCREEN_DOCUMENT
                    )
                ]
            });

        if (existingContexts.length > 0) {
            return;
        }
    }


    if (creatingOffscreenDocument) {
        await creatingOffscreenDocument;
        return;
    }


    creatingOffscreenDocument =
        chrome.offscreen.createDocument({
            url: OFFSCREEN_DOCUMENT,

            reasons: [
                "AUDIO_PLAYBACK"
            ],

            justification:
                "Play the LockIn warning sound when a restricted website is detected."
        });


    try {

        await creatingOffscreenDocument;

    } finally {

        creatingOffscreenDocument = null;

    }
}


// ================================
// PLAY WARNING SOUND
// ================================

async function playWarningSound(sound) {

    try {

        await setupOffscreenDocument();

        chrome.runtime.sendMessage({
            action: "playWarningSound",
            sound: sound
        });

    } catch (error) {

        console.log(
            "LockIn warning sound error:",
            error
        );

    }
}


// ================================
// MESSAGES FROM CONTENT.JS
// ================================

chrome.runtime.onMessage.addListener(
    function (
        message,
        sender,
        sendResponse
    ) {


        // ================================
        // GET RESTRICTED SITES
        // ================================

        if (
            message.action ===
            "getRestrictedSites"
        ) {

            fetch(
                "http://localhost:8080/restricted"
            )
            .then(response =>
                response.json()
            )
            .then(data => {

                sendResponse(data);

            })
            .catch(error => {

                console.log(
                    "LockIn restricted sites error:",
                    error
                );

                sendResponse([]);

            });

            return true;
        }


        // ================================
        // GET TIMER STATUS
        // ================================

        if (
            message.action ===
            "getTimerStatus"
        ) {

            fetch(
                "http://localhost:8080/timer"
            )
            .then(response =>
                response.json()
            )
            .then(data => {

                sendResponse(data);

            })
            .catch(error => {

                console.log(
                    "LockIn timer status error:",
                    error
                );

                sendResponse({
                    active: false,
                    mode: "unknown"
                });

            });

            return true;
        }


        // ================================
        // GET WARNING MESSAGE
        // ================================

        if (
            message.action ===
            "getWarningMessage"
        ) {

            fetch(
                "http://localhost:8080/message"
            )
            .then(response =>
                response.text()
            )
            .then(text => {

                sendResponse(text);

            })
            .catch(error => {

                console.log(
                    "LockIn warning message error:",
                    error
                );

                sendResponse(
                    "This website is on your distraction list."
                );

            });

            return true;
        }


        // ================================
        // GET SELECTED SOUND
        // ================================

        if (
            message.action ===
            "getSelectedSound"
        ) {

            fetch(
                "http://localhost:8080/sound"
            )
            .then(response =>
                response.text()
            )
            .then(text => {

                const sound =
                    Number.parseInt(
                        text,
                        10
                    );

                sendResponse(
                    sound === 1 || sound === 2
                        ? sound
                        : 2
                );

            })
            .catch(error => {

                console.log(
                    "LockIn selected sound error:",
                    error
                );

                sendResponse(2);

            });

            return true;
        }


        // ================================
        // PLAY WARNING SOUND
        // ================================

        if (
            message.action ===
            "playWarningSound"
        ) {

            const sound =
                message.sound === 1
                    ? 1
                    : 2;

            playWarningSound(sound);

            sendResponse({
                success: true
            });

            return true;
        }

    }
);
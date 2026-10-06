// Name of the hidden page used to play the warning sound.
const OFFSCREEN_DOCUMENT = "offscreen.html";

// Remembers if the offscreen page is still being created so we don't create it twice.
let creatingOffscreenDocument = null;



// TRACK ACTIVE TAB

// Sends the website to the Java app whenever the user switches to another tab.
chrome.tabs.onActivated.addListener(async (activeInfo) => {
    try {
        // Get the details of the tab the user just opened.
        const tab = await chrome.tabs.get(activeInfo.tabId);

        // Only send it if the tab has a URL.
        if (tab.url) {
            sendWebsite(tab.url);
        }
    } catch (error) {
        console.log("LockIn tab activation error:", error);
    }
});


// TRACK URL CHANGES + PAGE LOAD

// Sends the website to the Java app whenever a tab changes its URL or finishes loading.
chrome.tabs.onUpdated.addListener(
    (tabId, changeInfo, tab) => {

        // Send when the URL of the tab changed.
        if (
            changeInfo.url &&
            changeInfo.url === tab.url
        ) {
            sendWebsite(changeInfo.url);
        }

        // Send again when the page finishes loading.
        if (
            changeInfo.status === "complete" &&
            tab.url
        ) {
            sendWebsite(tab.url);
        }
    }
);


// SEND WEBSITE TO JAVA API

// Cleans the URL into a plain domain and posts it to the Java app running on localhost.
function sendWebsite(url) {

    try {

        // Skip empty URLs and browser-internal pages.
        if (
            !url ||
            url.startsWith("chrome://") ||
            url.startsWith("chrome-extension://") ||
            url.startsWith("edge://") ||
            url.startsWith("about:")
        ) {
            return;
        }

        // Get only the domain name (no "www.").
        const website =
            new URL(url).hostname
                .replace(/^www\./, "");

        // Stop if there is no domain.
        if (!website) {
            return;
        }

        // Send the domain to the Java server as plain text.
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

            // Print the server's reply for debugging.
            console.log(
                "LockIn website status:",
                website,
                data
            );

        })
        .catch(error => {

            // Print an error if the Java app can't be reached.
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


// OFFSCREEN DOCUMENT

// Makes sure the hidden offscreen page exists (creates it only if it's missing) so audio can play.
async function setupOffscreenDocument() {

    // Newer Chrome: ask directly if the offscreen page already exists.
    if (chrome.offscreen.hasDocument) {

        const hasDocument =
            await chrome.offscreen.hasDocument();

        if (hasDocument) {
            return;
        }

    } else {

        // Older Chrome: look for the offscreen page in the list of running contexts.
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


    // If another call is already creating it, just wait for that one to finish.
    if (creatingOffscreenDocument) {
        await creatingOffscreenDocument;
        return;
    }


    // Create the offscreen page, telling Chrome it is used for audio playback.
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

        // Wait until the page is ready.
        await creatingOffscreenDocument;

    } finally {

        // Reset the flag so it can be created again later if needed.
        creatingOffscreenDocument = null;

    }
}


// PLAY WARNING SOUND

// Opens the offscreen page and tells it which warning sound to play.
async function playWarningSound(sound) {

    try {

        // Make sure the offscreen page is running first.
        await setupOffscreenDocument();

        // Tell the offscreen page to play the chosen sound.
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


// MESSAGES FROM CONTENT.JS

// Listens for requests from content.js and answers them using data from the Java app.
chrome.runtime.onMessage.addListener(
    function (
        message,
        sender,
        sendResponse
    ) {


        // GET RESTRICTED SITES

        // Gets the list of restricted sites from the Java app and gives it to content.js.
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

                // If the Java app is unreachable, reply with an empty list.
                sendResponse([]);

            });

            // Keeps the message channel open for the async reply.
            return true;
        }


        // GET TIMER STATUS

        // Gets from the Java app whether the timer is running and which mode it is in.
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

                // If the Java app is unreachable, reply that the timer is not active.
                sendResponse({
                    active: false,
                    mode: "unknown"
                });

            });

            return true;
        }


        // GET WARNING MESSAGE

        // Gets the warning message chosen in Settings from the Java app.
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

                // Use a default message if the Java app is unreachable.
                sendResponse(
                    "This website is on your distraction list."
                );

            });

            return true;
        }


        // GET SELECTED SOUND

        // Gets which warning sound (1 or 2) the user picked in the Java app.
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

                // Turn the text reply into a number.
                const sound =
                    Number.parseInt(
                        text,
                        10
                    );

                // Only accept 1 or 2, otherwise use sound 2.
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

                // Default to sound 2 if the Java app is unreachable.
                sendResponse(2);

            });

            return true;
        }


        // PLAY WARNING SOUND

        // Plays the warning sound when content.js asks for it.
        if (
            message.action ===
            "playWarningSound"
        ) {

            // Only allow sound 1 or 2 (anything else becomes 2).
            const sound =
                message.sound === 1
                    ? 1
                    : 2;

            playWarningSound(sound);

            // Tell content.js the request was received.
            sendResponse({
                success: true
            });

            return true;
        }

    }
);
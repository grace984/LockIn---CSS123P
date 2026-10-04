chrome.tabs.onActivated.addListener(async (activeInfo) => {

    try {

        const tab =
            await chrome.tabs.get(
                activeInfo.tabId
            );

        if (tab.url) {
            sendWebsite(tab.url);
        }

    } catch (error) {

        console.log(error);

    }
});


chrome.tabs.onUpdated.addListener(
    (tabId, changeInfo) => {

        if (changeInfo.url) {
            sendWebsite(changeInfo.url);
        }

    }
);


function sendWebsite(url) {

    try {

        const website =
            new URL(url).hostname;

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

        console.log(error);

    }
}


chrome.runtime.onMessage.addListener(
    function (
        message,
        sender,
        sendResponse
    ) {

        // GET RESTRICTED SITES
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


        // GET TIMER STATUS
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


        // GET WARNING MESSAGE
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


        // GET SELECTED SOUND
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
    }
);
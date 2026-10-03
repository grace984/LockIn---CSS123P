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
                    "Content-Type":
                        "text/plain"
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
    }
);
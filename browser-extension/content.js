let warningVisible = false;
let timerWasActive = false;


function checkLockInStatus() {

    chrome.runtime.sendMessage(
        {
            action: "getTimerStatus"
        },
        function (timerStatus) {

            if (chrome.runtime.lastError) {

                console.log(
                    "LockIn timer error:",
                    chrome.runtime.lastError.message
                );

                return;
            }


            // WORK TIMER IS NOT RUNNING
            if (
                !timerStatus ||
                timerStatus.active !== true ||
                timerStatus.mode !== "work"
            ) {

                timerWasActive = false;

                removeWarning();

                return;
            }


            // WORK TIMER IS RUNNING
            timerWasActive = true;

            checkRestrictedSite();

        }
    );
}


function checkRestrictedSite() {

    chrome.runtime.sendMessage(
        {
            action: "getRestrictedSites"
        },
        function (restrictedSites) {

            if (chrome.runtime.lastError) {

                console.log(
                    "LockIn restricted sites error:",
                    chrome.runtime.lastError.message
                );

                return;
            }


            if (!restrictedSites) {
                return;
            }


            const currentHostname =
                window.location.hostname
                    .toLowerCase()
                    .replace(/^www\./, "");


            if (!currentHostname) {
                return;
            }


            const isRestricted =
                restrictedSites.some(function (site) {

                    const cleanSite =
                        String(site)
                            .toLowerCase()
                            .replace(/^www\./, "");

                    return (
                        currentHostname === cleanSite ||
                        currentHostname.endsWith(
                            "." + cleanSite
                        )
                    );

                });


            // WEBSITE IS NOT ON DISTRACTION LIST
            if (!isRestricted) {

                removeWarning();

                return;
            }


            // WEBSITE IS RESTRICTED
            showWarning(currentHostname);

        }
    );
}


function showWarning(currentHostname) {

    if (
        document.getElementById(
            "lockin-warning"
        )
    ) {
        return;
    }


    const overlay =
        document.createElement("div");

    overlay.id =
        "lockin-warning";


    overlay.innerHTML = `
        <div id="lockin-warning-box">

            <div class="lockin-title">
                LockIn Warning
            </div>

            <div class="lockin-message">
                This website is on your
                distraction list.
            </div>

            <div class="lockin-site">
                ${currentHostname}
            </div>

            <button id="lockin-close">
                Continue
            </button>

        </div>
    `;


    const style =
        document.createElement("style");


    style.id =
        "lockin-warning-style";


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
            width: 360px;
            padding: 30px;
            border-radius: 15px;
            text-align: center;
            box-shadow:
                0 8px 30px rgba(0, 0, 0, 0.35);
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

    document.documentElement.appendChild(
        overlay
    );


    const closeButton =
        document.getElementById(
            "lockin-close"
        );


    if (closeButton) {

        closeButton.addEventListener(
            "click",
            function () {

                overlay.remove();

                style.remove();

            }
        );

    }

}


function removeWarning() {

    const overlay =
        document.getElementById(
            "lockin-warning"
        );

    if (overlay) {
        overlay.remove();
    }


    const style =
        document.getElementById(
            "lockin-warning-style"
        );

    if (style) {
        style.remove();
    }

}


// CHECK IMMEDIATELY
checkLockInStatus();


// KEEP CHECKING TIMER STATUS
// This allows the website to react when
// Work starts, pauses, or enters break.
setInterval(
    checkLockInStatus,
    2000
);
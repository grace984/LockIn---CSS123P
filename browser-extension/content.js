chrome.runtime.sendMessage(
    { action: "getRestrictedSites" },
    function (restrictedSites) {

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

        if (!isRestricted) {
            return;
        }

        if (
            document.getElementById(
                "lockin-warning"
            )
        ) {
            return;
        }

        const overlay =
            document.createElement("div");

        overlay.id = "lockin-warning";

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

        document
            .getElementById("lockin-close")
            .addEventListener("click", function () {
                overlay.remove();
                style.remove();
            });
    }
);
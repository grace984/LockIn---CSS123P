setInterval(async () => {
    try {
        const tabs = await chrome.tabs.query({ active: true, currentWindow: true });
        if (!tabs || tabs.length === 0) return;

        const activeTab = tabs[0];
        if (!activeTab.url || !activeTab.url.startsWith('http')) return;

        const currentUrl = new URL(activeTab.url).hostname;

        const response = await fetch(
            `http://localhost:8080/check?url=${encodeURIComponent(currentUrl)}`
        );
        const data = await response.json();

        if (data.restricted && data.isWork) {
            await chrome.scripting.executeScript({
                target: { tabId: activeTab.id },
                func: injectWarningOverlay,
                args: [data.remaining]
            });
        }
    } catch (e) {
        // Server offline or page not injectable (e.g. chrome:// pages) - ignore
    }
}, 3000);

// This function runs inside the web page, so it must be fully self-contained.
function injectWarningOverlay(remainingText) {
    const existingWarning = document.getElementById('lockin-warning-overlay');

    if (existingWarning) {
        const timeEl = document.getElementById('lockin-time-remaining');
        if (timeEl) timeEl.textContent = remainingText;
        return;
    }

    const overlay = document.createElement('div');
    overlay.id = 'lockin-warning-overlay';
    overlay.style.cssText = `
        position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
        background: rgba(89, 19, 44, 0.85); z-index: 2147483647;
        display: flex; align-items: center; justify-content: center;
        font-family: 'Times New Roman', serif; color: #FFF5E4;
    `;

    overlay.innerHTML = `
        <div style="text-align: center; max-width: 500px; padding: 40px;">
            <div style="font-size: 14px; letter-spacing: 6px; margin-bottom: 20px;">LOCKIN</div>
            <h1 style="font-size: 48px; margin: 0 0 16px;">⚠ Stay focused!</h1>
            <p style="font-size: 20px; margin: 0 0 12px;">This website is restricted during your work session.</p>
            <p id="lockin-time-remaining" style="font-size: 18px; opacity: 0.8; margin: 0 0 28px;"></p>
            <button id="lockin-pause-btn" style="
                padding: 12px 28px; font-size: 16px; font-family: inherit;
                background: #FFF5E4; color: #59132C; border: none;
                border-radius: 6px; cursor: pointer;">Pause Timer</button>
        </div>
    `;

    document.body.appendChild(overlay);

    // Set via textContent so server text is never parsed as HTML
    document.getElementById('lockin-time-remaining').textContent = remainingText;

    document.getElementById('lockin-pause-btn').addEventListener('click', async () => {
        try {
            await fetch('http://localhost:8080/pause', { method: 'POST' });
        } catch (e) {
            // ignore network errors
        }
        overlay.remove();
    });
}
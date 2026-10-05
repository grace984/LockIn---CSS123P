chrome.runtime.onMessage.addListener((message) => {
    if (message.action !== "playWarningSound") {
        return;
    }

    const sound = message.sound === 1 ? 1 : 2;

    playWarningSound(sound);
});


function playWarningSound(sound) {
    try {
        const AudioContextClass =
            window.AudioContext ||
            window.webkitAudioContext;

        if (!AudioContextClass) {
            console.log(
                "LockIn: AudioContext is not available."
            );
            return;
        }

        const audioContext =
            new AudioContextClass();

        if (audioContext.state === "suspended") {
            audioContext.resume();
        }

        playBeep(audioContext);

        if (sound === 2) {
            setTimeout(() => {
                playBeep(audioContext);
            }, 250);
        }

        setTimeout(() => {
            audioContext.close();
        }, sound === 2 ? 600 : 350);

    } catch (error) {
        console.log(
            "LockIn offscreen audio error:",
            error
        );
    }
}


function playBeep(audioContext) {
    const oscillator =
        audioContext.createOscillator();

    const gainNode =
        audioContext.createGain();

    oscillator.type = "square";

    oscillator.frequency.setValueAtTime(
        1000,
        audioContext.currentTime
    );

    gainNode.gain.setValueAtTime(
        0.45,
        audioContext.currentTime
    );

    gainNode.gain.exponentialRampToValueAtTime(
        0.01,
        audioContext.currentTime + 0.18
    );

    oscillator.connect(gainNode);

    gainNode.connect(
        audioContext.destination
    );

    oscillator.start();

    oscillator.stop(
        audioContext.currentTime + 0.18
    );
}
let audioContext = null;
let isPlaying = false;


// ================================
// RECEIVE SOUND REQUEST
// ================================

chrome.runtime.onMessage.addListener((message) => {

    if (message.action !== "playWarningSound") {
        return;
    }

    const sound =
        message.sound === 1
            ? 1
            : 2;

    playWarningSound(sound);
});


// ================================
// GET / CREATE AUDIO CONTEXT
// ================================

async function getAudioContext() {

    const AudioContextClass =
        window.AudioContext ||
        window.webkitAudioContext;

    if (!AudioContextClass) {
        console.log(
            "LockIn: AudioContext is not available."
        );

        return null;
    }

    if (!audioContext) {
        audioContext =
            new AudioContextClass();
    }

    if (audioContext.state === "suspended") {
        try {
            await audioContext.resume();
        } catch (error) {
            console.log(
                "LockIn audio resume error:",
                error
            );

            return null;
        }
    }

    return audioContext;
}


// ================================
// PLAY WARNING SOUND
// ================================

async function playWarningSound(sound) {

    // Prevent overlapping warning sounds
    if (isPlaying) {
        console.log(
            "LockIn: warning sound already playing."
        );

        return;
    }

    isPlaying = true;

    try {

        const context =
            await getAudioContext();

        if (!context) {
            return;
        }


        // ================================
        // SOUND 1
        // ================================

        playBeep(context);


        // ================================
        // SOUND 2
        // ================================

        if (sound === 2) {

            await wait(250);

            playBeep(context);
        }


        // Wait until the final beep finishes
        await wait(250);

    } catch (error) {

        console.log(
            "LockIn offscreen audio error:",
            error
        );

    } finally {

        isPlaying = false;
    }
}


// ================================
// PLAY ONE BEEP
// ================================

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


    oscillator.connect(
        gainNode
    );


    gainNode.connect(
        audioContext.destination
    );


    oscillator.start();


    oscillator.stop(
        audioContext.currentTime + 0.18
    );
}


// ================================
// WAIT
// ================================

function wait(milliseconds) {

    return new Promise((resolve) => {

        setTimeout(
            resolve,
            milliseconds
        );

    });
}
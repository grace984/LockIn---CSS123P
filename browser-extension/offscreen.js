// Stores the audio context used to create sounds
let audioContext = null; 

// Keeps track of whether a warning sound is currently playing
let isPlaying = false; 

 
// Listens for messages from other parts of the extension
chrome.runtime.onMessage.addListener((message) => { 
 
    // Ignores messages that are not for playing the warning sound
    if (message.action !== "playWarningSound") { 
        return; 
    } 
 
    // Selects which warning sound should be played
    const sound = 
        message.sound === 1 
            ? 1 
            : 2; 
 
    // Plays the selected warning sound
    playWarningSound(sound); 
}); 
 
 
// Gets the existing audio context or creates a new one
async function getAudioContext() { 
 
    // Gets the browser's available AudioContext class
    const AudioContextClass = 
        window.AudioContext || 
        window.webkitAudioContext; 
 
    // Checks if AudioContext is not supported by the browser
    if (!AudioContextClass) { 
        console.log( 
            "LockIn: AudioContext is not available." 
        ); 
 
        return null; 
    } 
 
    // Creates the audio context if one does not exist yet
    if (!audioContext) { 
        audioContext = 
            new AudioContextClass(); 
    } 
 
    // Resumes the audio context if it is currently suspended
    if (audioContext.state === "suspended") { 
        try { 
            await audioContext.resume(); 
        } catch (error) { 
            // Displays an error if the audio context cannot resume
            console.log( 
                "LockIn audio resume error:", 
                error 
            ); 
 
            return null; 
        } 
    } 
 
    // Returns the ready audio context
    return audioContext; 
} 

 
// Plays the warning sound selected by the tracker
async function playWarningSound(sound) { 
 
    // Prevent overlapping warning sounds 
    if (isPlaying) { 
        console.log( 
            "LockIn: warning sound already playing." 
        ); 
 
        return; 
    } 
 
    // Marks the warning sound as currently playing
    isPlaying = true; 
 
    try { 
 
        // Gets the audio context needed to play the sound
        const context = 
            await getAudioContext(); 
 
        // Stops if the audio context is not available
        if (!context) { 
            return; 
        } 
 
        // Plays the first beep
        playBeep(context); 
 
        // Plays a second beep when sound 2 is selected
        if (sound === 2) { 
 
            // Waits before playing the second beep
            await wait(250); 
 
            playBeep(context); 
        } 
 
 
        // Wait until the final beep finishes 
        await wait(250); 
 
    } catch (error) { 
 
        // Displays an error if the warning sound cannot play
        console.log( 
            "LockIn offscreen audio error:", 
            error 
        ); 
 
    } finally { 
 
        // Allows another warning sound to play
        isPlaying = false; 
    } 
} 
 
// Creates and plays one beep sound
function playBeep(audioContext) { 
 
    // Creates the oscillator that produces the sound
    const oscillator = 
        audioContext.createOscillator(); 
 
    // Creates the gain node that controls the volume
    const gainNode = 
        audioContext.createGain(); 
 
 
    // Sets the type of sound wave used for the beep
    oscillator.type = "square"; 
 
 
    // Sets the beep frequency
    oscillator.frequency.setValueAtTime( 
        1000, 
        audioContext.currentTime 
    ); 
 
 
    // Sets the starting volume of the beep
    gainNode.gain.setValueAtTime( 
        0.45, 
        audioContext.currentTime 
    ); 
 
 
    // Gradually lowers the volume so the beep fades out
    gainNode.gain.exponentialRampToValueAtTime( 
        0.01, 
        audioContext.currentTime + 0.18 
    ); 
 
 
    // Connects the oscillator to the volume controller
    oscillator.connect( 
        gainNode 
    ); 
 
 
    // Connects the volume controller to the browser's audio output
    gainNode.connect( 
        audioContext.destination 
    ); 
 
 
    // Starts the beep
    oscillator.start(); 
 
 
    // Stops the beep after a short amount of time
    oscillator.stop( 
        audioContext.currentTime + 0.18 
    ); 
} 
 
 
// Creates a delay before the next action
function wait(milliseconds) { 
 
    // Returns a promise that finishes after the given time
    return new Promise((resolve) => { 
 
        // Waits for the specified number of milliseconds
        setTimeout( 
            resolve, 
            milliseconds 
        ); 
 
    }); 
}
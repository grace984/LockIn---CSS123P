package lockin; 

// Swing classes for timers and event handling
import javax.swing.Timer; 
import javax.swing.SwingUtilities; 

// AWT class used for color animations
import java.awt.Color; 

// Used for handling animation values
import java.util.function.DoubleConsumer; 
import java.util.function.DoubleUnaryOperator; 

// Utility class for handling animations in LockIn
public final class AnimationUtils { 

    // Delay between each animation frame
    private static final int FRAME_DELAY_MS = 16; 

    // Prevents creating objects from this utility class
    private AnimationUtils() { } 

    // Creates a smooth animation that speeds up and slows down
    public static double easeInOut(double progress) { 
        double t = clamp(progress); 
        return t * t * (3.0 - 2.0 * t); 
    } 

    // Creates an animation that starts quickly and slows down
    public static double easeOut(double progress) { 
        double t = clamp(progress); 
        return 1.0 - (1.0 - t) * (1.0 - t); 
    } 

    // Keeps the value between 0 and 1
    public static double clamp(double value) { 
        return Math.max(0.0, Math.min(1.0, value)); 
    } 

    // Calculates a value between two numbers
    public static double interpolate(double from, double to, double progress) { 
        return from + (to - from) * clamp(progress); 
    } 

    // Gradually changes one color into another
    public static Color interpolate(Color from, Color to, double progress) { 
        double t = clamp(progress); 
        return new Color( 
                (int) Math.round(interpolate(from.getRed(), to.getRed(), t)), 
                (int) Math.round(interpolate(from.getGreen(), to.getGreen(), t)), 
                (int) Math.round(interpolate(from.getBlue(), to.getBlue(), t)), 
                (int) Math.round(interpolate(from.getAlpha(), to.getAlpha(), t))); 
    } 
 

    // Starts and controls a short animation
    public static Animation animate(int durationMs, DoubleUnaryOperator easing, 
                                    DoubleConsumer update, Runnable completion) { 

        // Makes sure the animation runs on Swing's event thread
        if (!SwingUtilities.isEventDispatchThread()) { 
            throw new IllegalStateException("Swing animations must start on the EDT"); 
        } 

        // Creates an animation object
        Animation animation = new Animation(); 

        // Calculates the animation duration
        long durationNanos = Math.max(1, durationMs) * 1_000_000L; 

        // Saves the starting time of the animation
        long startedAt = System.nanoTime(); 

        // Starts the animation at zero progress
        update.accept(0.0); 

        // Updates the animation based on the timer
        animation.timer = new Timer(FRAME_DELAY_MS, event -> { 
            double rawProgress = Math.min(1.0, (System.nanoTime() - startedAt) / (double) durationNanos); 
            update.accept(easing.applyAsDouble(rawProgress)); 

            // Stops the animation when it reaches the end
            if (rawProgress >= 1.0) { 
                animation.stop(); 

                // Runs the completion action after the animation
                if (completion != null) { 
                    completion.run(); 
                } 
            } 
        }); 

        // Prevents too many timer events from building up
        animation.timer.setCoalesce(true); 

        // Starts the animation timer
        animation.timer.start(); 

        return animation; 
    } 

    // Represents a running animation
    public static final class Animation { 
        private Timer timer; 

        // Creates an Animation object internally
        private Animation() { } 

        // Stops the current animation
        public void stop() { 
            if (timer != null) { 
                timer.stop(); 
                timer = null; 
            } 
        } 

        // Checks if the animation is still running
        public boolean isRunning() { 
            return timer != null && timer.isRunning(); 
        } 
    } 
} 
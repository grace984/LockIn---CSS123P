package lockin;

import javax.swing.Timer;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleUnaryOperator;

/** Small EDT-only animation primitives shared by LockIn Swing components. */
public final class AnimationUtils {
    private static final int FRAME_DELAY_MS = 16;

    private AnimationUtils() { }

    public static double easeInOut(double progress) {
        double t = clamp(progress);
        return t * t * (3.0 - 2.0 * t);
    }

    public static double easeOut(double progress) {
        double t = clamp(progress);
        return 1.0 - (1.0 - t) * (1.0 - t);
    }

    public static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    public static double interpolate(double from, double to, double progress) {
        return from + (to - from) * clamp(progress);
    }

    public static Color interpolate(Color from, Color to, double progress) {
        double t = clamp(progress);
        return new Color(
                (int) Math.round(interpolate(from.getRed(), to.getRed(), t)),
                (int) Math.round(interpolate(from.getGreen(), to.getGreen(), t)),
                (int) Math.round(interpolate(from.getBlue(), to.getBlue(), t)),
                (int) Math.round(interpolate(from.getAlpha(), to.getAlpha(), t)));
    }

    /**
     * Runs a short animation on Swing's event thread. The update receives eased
     * progress from 0 to 1. The returned handle can stop an animation early.
     */
    public static Animation animate(int durationMs, DoubleUnaryOperator easing,
                                    DoubleConsumer update, Runnable completion) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Swing animations must start on the EDT");
        }

        Animation animation = new Animation();
        long durationNanos = Math.max(1, durationMs) * 1_000_000L;
        long startedAt = System.nanoTime();
        update.accept(0.0);
        animation.timer = new Timer(FRAME_DELAY_MS, event -> {
            double rawProgress = Math.min(1.0, (System.nanoTime() - startedAt) / (double) durationNanos);
            update.accept(easing.applyAsDouble(rawProgress));
            if (rawProgress >= 1.0) {
                animation.stop();
                if (completion != null) {
                    completion.run();
                }
            }
        });
        animation.timer.setCoalesce(true);
        animation.timer.start();
        return animation;
    }

    public static final class Animation {
        private Timer timer;

        private Animation() { }

        public void stop() {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
        }

        public boolean isRunning() {
            return timer != null && timer.isRunning();
        }
    }
}

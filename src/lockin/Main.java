package lockin;

// Lets us run code on Swing's UI thread
import javax.swing.SwingUtilities;

// Entry point: creates the shared panels, starts the server, and opens the window
public class Main {

    // Java starts running the app here
    public static void main(String[] args) {

        // Create ONE tracker panel (the restricted-sites list and log)
        ramiraTrackerPanel trackerPanel =
                new ramiraTrackerPanel();

        // Create ONE timer panel (the Work/Break countdown)
        lauriceTimerPanel timerPanel =
                new lauriceTimerPanel();

        /*
         * Give the SAME tracker panel and
         * SAME timer panel to the API server,
         * so it checks the live list and timer.
         */
        jhenicaApiServer apiServer =
                new jhenicaApiServer(
                        trackerPanel,
                        timerPanel
                );

        // The server may fail to start (for example, if the port is in use)
        try {

            // Start listening for websites sent by the browser extension
            apiServer.start();

        } catch (Exception e) {

            // Print the error but keep the app running
            e.printStackTrace();

        }

        // Build the window on the UI thread, as Swing requires
        SwingUtilities.invokeLater(() -> {

            /*
             * Give the SAME tracker panel and
             * SAME timer panel to the UI,
             * so the screen and the server share them.
             */
            // Create the main window and show it
            new MainFrame(
                    trackerPanel,
                    timerPanel
            ).setVisible(true);

        });
    }
}
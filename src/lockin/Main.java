package lockin;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        // Create ONE tracker panel
        ramiraTrackerPanel trackerPanel =
                new ramiraTrackerPanel();

        // Create ONE timer panel
        lauriceTimerPanel timerPanel =
                new lauriceTimerPanel();

        /*
         * Give the SAME tracker panel and
         * SAME timer panel to the API server.
         */
        jhenicaApiServer apiServer =
                new jhenicaApiServer(
                        trackerPanel,
                        timerPanel
                );

        try {

            apiServer.start();

        } catch (Exception e) {

            e.printStackTrace();

        }

        SwingUtilities.invokeLater(() -> {

            /*
             * Give the SAME tracker panel and
             * SAME timer panel to the UI.
             */
            new MainFrame(
                    trackerPanel,
                    timerPanel
            ).setVisible(true);

        });
    }
}
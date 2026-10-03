package lockin;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        // Create the tracker panel
        ramiraTrackerPanel trackerPanel =
                new ramiraTrackerPanel();

        // Give the same tracker panel to the API server
        jhenicaApiServer apiServer =
                new jhenicaApiServer(
                        trackerPanel
                );

        try {

            apiServer.start();

        } catch (Exception e) {

            e.printStackTrace();

        }

        SwingUtilities.invokeLater(() -> {

            new MainFrame().setVisible(true);

        });
    }
}
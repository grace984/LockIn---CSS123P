package lockin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class jhenicaApiServer {

    private HttpServer server;
    private String currentWebsite = "";

    private ramiraTrackerPanel trackerPanel;

    /*
     * Timer panel reference.
     *
     * This allows the API server to know
     * whether the Work timer is currently running.
     */
    private lauriceTimerPanel timerPanel;

    public jhenicaApiServer(
            ramiraTrackerPanel trackerPanel,
            lauriceTimerPanel timerPanel
    ) {

        this.trackerPanel = trackerPanel;
        this.timerPanel = timerPanel;
    }

    public void start() throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(8080),
                0
        );

        /*
         * Chrome sends the currently opened website here.
         */
        server.createContext(
                "/website",
                this::handleWebsite
        );

        /*
         * Chrome gets the user's restricted sites here.
         */
        server.createContext(
                "/restricted",
                this::handleRestricted
        );

        /*
         * Chrome checks whether Work timer is active here.
         */
        server.createContext(
                "/timer",
                this::handleTimer
        );

        /*
         * Chrome gets the warning message chosen in Settings here.
         */
        server.createContext(
                "/message",
                this::handleMessage
        );

        server.setExecutor(null);

        server.start();

        System.out.println(
                "API server running on port 8080"
        );
    }

   
     // WEBSITE ENDPOINT
   

    private void handleWebsite(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (
                "OPTIONS".equalsIgnoreCase(
                        exchange.getRequestMethod()
                )
        ) {

            exchange.sendResponseHeaders(
                    204,
                    -1
            );

            exchange.close();

            return;
        }

        if (
                "POST".equalsIgnoreCase(
                        exchange.getRequestMethod()
                )
        ) {

            currentWebsite =
                    new String(
                            exchange
                                    .getRequestBody()
                                    .readAllBytes(),
                            StandardCharsets.UTF_8
                    ).trim();

            System.out.println(
                    "Current website: "
                            + currentWebsite
            );

            /*
             * Restriction only applies when
             * the Work timer is actively running.
             */
            boolean workTimerActive =
                    timerPanel != null
                            && timerPanel
                            .isWorkTimerActive();

            /*
             * First check if the website is
             * actually on the user's distraction list.
             */
            boolean restricted =
                    trackerPanel != null
                            && trackerPanel.isRestricted(
                            currentWebsite
                    );

            /*
             * Website is considered actively restricted
             * ONLY when:
             *
             * 1. It is on the distraction list
             * 2. Work timer is running
             */
            boolean activeRestriction =
                    restricted
                            && workTimerActive;

            if (activeRestriction) {

                System.out.println(
                        "RESTRICTED WEBSITE DETECTED "
                                + "DURING WORK: "
                                + currentWebsite
                );

                trackerPanel.logTrackedSite(
                        currentWebsite
                );
            }

            /*
             * If the timer is not running,
             * the website is allowed normally.
             */
            String response =
                    activeRestriction
                            ? "restricted"
                            : "allowed";

            sendResponse(
                    exchange,
                    response
            );

            return;
        }

        exchange.sendResponseHeaders(
                405,
                -1
        );

        exchange.close();
    }

    /*
     * =========================================================
     * RESTRICTED SITES ENDPOINT
     * =========================================================
     */

    private void handleRestricted(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (
                "OPTIONS".equalsIgnoreCase(
                        exchange.getRequestMethod()
                )
        ) {

            exchange.sendResponseHeaders(
                    204,
                    -1
            );

            exchange.close();

            return;
        }

        if (
                !"GET".equalsIgnoreCase(
                        exchange.getRequestMethod()
                )
        ) {

            exchange.sendResponseHeaders(
                    405,
                    -1
            );

            exchange.close();

            return;
        }

        StringBuilder json =
                new StringBuilder();

        json.append("[");

        boolean first = true;

        for (
                String site :
                ramiraTrackerPanel.restrictedSites
        ) {

            if (!first) {
                json.append(",");
            }

            json.append("\"");

            json.append(
                    escapeJson(site)
            );

            json.append("\"");

            first = false;
        }

        json.append("]");

        sendResponse(
                exchange,
                json.toString()
        );
    }

    /*
     * =========================================================
     * TIMER STATUS ENDPOINT
     * =========================================================
     *
     * Chrome can call:
     *
     * http://localhost:8080/timer
     *
     * Example response while Work timer is running:
     *
     * {"active":true,"mode":"work"}
     *
     * Example response during Break:
     *
     * {"active":false,"mode":"break"}
     *
     */

    private void handleTimer(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (
                "OPTIONS".equalsIgnoreCase(
                        exchange.getRequestMethod()
                )
        ) {

            exchange.sendResponseHeaders(
                    204,
                    -1
            );

            exchange.close();

            return;
        }

        if (
                !"GET".equalsIgnoreCase(
                        exchange.getRequestMethod()
                )
        ) {

            exchange.sendResponseHeaders(
                    405,
                    -1
            );

            exchange.close();

            return;
        }

        boolean active = false;

        String mode = "none";

        if (timerPanel != null) {

            /*
             * TRUE only when Work timer
             * is actually running.
             */
            active =
                    timerPanel.isWorkTimerActive();

            if (
                    timerPanel.isWorkSession()
            ) {

                mode = "work";

            } else if (
                    timerPanel.isBreakSession()
            ) {

                mode = "break";
            }
        }

        String response =
                "{"
                        + "\"active\":"
                        + active
                        + ","
                        + "\"mode\":\""
                        + mode
                        + "\""
                        + "}";

        sendResponse(
                exchange,
                response
        );
    }

    /*
     * =========================================================
     * WARNING MESSAGE ENDPOINT
     * =========================================================
     *
     * Chrome can call:
     *
     * http://localhost:8080/message
     *
     * It returns the warning message the user picked
     * in Settings, as plain text.
     */

    private void handleMessage(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (
                "OPTIONS".equalsIgnoreCase(
                        exchange.getRequestMethod()
                )
        ) {

            exchange.sendResponseHeaders(
                    204,
                    -1
            );

            exchange.close();

            return;
        }

        if (
                !"GET".equalsIgnoreCase(
                        exchange.getRequestMethod()
                )
        ) {

            exchange.sendResponseHeaders(
                    405,
                    -1
            );

            exchange.close();

            return;
        }

        sendResponse(
                exchange,
                ramiraTrackerPanel.getWarningMessage()
        );
    }

    /*
     * =========================================================
     * CORS
     * =========================================================
     */

    private void addCorsHeaders(
            HttpExchange exchange
    ) {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, OPTIONS"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type"
        );
    }

    /*
     * =========================================================
     * SEND RESPONSE
     * =========================================================
     */

    private void sendResponse(
            HttpExchange exchange,
            String response
    ) throws IOException {

        byte[] data =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.sendResponseHeaders(
                200,
                data.length
        );

        try (
                OutputStream output =
                        exchange.getResponseBody()
        ) {

            output.write(data);
        }
    }

    /*
     * =========================================================
     * JSON ESCAPE
     * =========================================================
     */

    private String escapeJson(
            String text
    ) {

        return text
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                );
    }

    /*
     * =========================================================
     * GET CURRENT WEBSITE
     * =========================================================
     */

    public String getCurrentWebsite() {

        return currentWebsite;
    }

    /*
     * =========================================================
     * STOP SERVER
     * =========================================================
     */

    public void stop() {

        if (server != null) {

            server.stop(0);
        }
    }
}
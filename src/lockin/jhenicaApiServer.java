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

    public jhenicaApiServer(ramiraTrackerPanel trackerPanel) {
        this.trackerPanel = trackerPanel;
    }

    public void start() throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(8080),
                0
        );

        // Chrome sends opened website here
        server.createContext(
                "/website",
                this::handleWebsite
        );

        // Chrome gets the user's restricted sites here
        server.createContext(
                "/restricted",
                this::handleRestricted
        );

        server.setExecutor(null);
        server.start();

        System.out.println(
                "API server running on port 8080"
        );
    }



    private void handleWebsite(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(
                    204,
                    -1
            );

            exchange.close();
            return;
        }

        if ("POST".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            currentWebsite = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            ).trim();

            System.out.println(
                    "Current website: "
                            + currentWebsite
            );

            boolean restricted =
                    trackerPanel != null
                            && trackerPanel.isRestricted(
                            currentWebsite
                    );

            if (restricted) {

                System.out.println(
                        "RESTRICTED WEBSITE DETECTED: "
                                + currentWebsite
                );

                trackerPanel.logTrackedSite(
                        currentWebsite
                );
            }

            String response =
                    restricted
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



    private void handleRestricted(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(
                    204,
                    -1
            );

            exchange.close();
            return;
        }

        if (!"GET".equalsIgnoreCase(
                exchange.getRequestMethod())) {

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

        for (String site :
                ramiraTrackerPanel.restrictedSites) {

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

        try (OutputStream output =
                     exchange.getResponseBody()) {

            output.write(data);
        }
    }



    private String escapeJson(
            String text
    ) {

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    public String getCurrentWebsite() {
        return currentWebsite;
    }

    public void stop() {

        if (server != null) {
            server.stop(0);
        }
    }
}
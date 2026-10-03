package lockin;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class jhenicaApiServer {
    private HttpServer server; 
    private lauriceTimerPanel timerPanel;
    private ramiraTrackerPanel trackerPanel;

    public jhenicaApiServer(lauriceTimerPanel timerPanel, ramiraTrackerPanel trackerPanel) {
        this.timerPanel = timerPanel;
        this.trackerPanel = trackerPanel;
    }

    public void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            
            server.createContext("/check", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                    if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                        exchange.sendResponseHeaders(204, -1);
                        return;
                    }

                    String query = exchange.getRequestURI().getQuery();
                    String currentUrl = "";
                    if (query != null && query.contains("url=")) {
                        currentUrl = query.split("url=")[1];
                    }

                    boolean isWork = timerPanel.isWorkSession() && timerPanel.isTimerRunning();
                    boolean restricted = false;

                    if (isWork && !currentUrl.isEmpty()) {
                        restricted = trackerPanel.isRestricted(currentUrl);
                    }

                    int timeLeft = timerPanel.getTimeLeft();
                    String remainingText = String.format("%02d:%02d mins remaining", timeLeft / 60, timeLeft % 60);

                    String jsonResponse = String.format(
                        "{\"restricted\": %b, \"remaining\": \"%s\", \"isWork\": %b}",
                        restricted, remainingText, isWork
                    );

                    byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseBytes);
                    os.close();
                }
            });

            server.createContext("/pause", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    
                    if (exchange.getRequestMethod().equalsIgnoreCase("POST") || exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                        timerPanel.pauseTimer();
                        
                        String response = "{\"status\": \"success\", \"message\": \"Timer paused\"}";
                        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, bytes.length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(bytes);
                        os.close();
                    }
                }
            });

            server.setExecutor(null);
            server.start();
            System.out.println("Jhenica's API Server started on http://localhost:8080");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("API Server stopped.");
        }
    }
}
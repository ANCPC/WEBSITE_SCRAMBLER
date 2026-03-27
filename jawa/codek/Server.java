import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Server {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Serve HTML
        server.createContext("/", exchange -> {
            try (InputStream is = new FileInputStream("index.html")) {
                String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().add("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);

            } catch (Exception e) {
                System.err.println("Error serving HTML: " + e.getMessage());
            }
            exchange.close();
        });

        // Search API
        server.createContext("/search", (HttpExchange exchange) -> {
            try {
                String query = exchange.getRequestURI().getQuery();
                String topic = "AI";
                List<String> selectedSites = new ArrayList<>();

                if (query != null) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        if (param.startsWith("topic=")) {
                            topic = URLDecoder.decode(param.substring(6), "UTF-8");
                        } else if (param.startsWith("sites=")) {
                            String sites = URLDecoder.decode(param.substring(6), "UTF-8");
                            for (String site : sites.split(",")) {
                                selectedSites.add(site.trim());
                            }
                        }
                    }
                }

                List<String> results = new ArrayList<>();
                
                for (String site : selectedSites) {
                    String result = null;
                    switch (site.toLowerCase()) {
                        case "wikipedia":
                            result = GetData.getWikipedia(topic);
                            break;
                        case "hackernews":
                            result = GetData.getHackerNews(topic);
                            break;
                        case "stackoverflow":
                            result = GetData.getStackOverflow(topic);
                            break;
                        case "github":
                            result = GetData.getGitHub(topic);
                            break;
                        case "arxiv":
                            result = GetData.getArXiv(topic);
                            break;
                        case "reddit":
                            result = GetData.getReddit(topic);
                            break;
                    }
                    if (result != null) {
                        results.add(result);
                    }
                }

                String resultJson = "{ \"results\": [" + String.join(",", results) + "] }";
                byte[] bytes = resultJson.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);

            } catch (IOException | RuntimeException e) {
                try {
                    String error = "{ \"error\": \"Failed\" }";
                    byte[] bytes = error.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(500, bytes.length);
                    exchange.getResponseBody().write(bytes);
                } catch (IOException ex) {
                    System.err.println("Error sending error response: " + ex.getMessage());
                }
            }

            exchange.close();
        });

        // API to get available websites
        server.createContext("/websites", (HttpExchange exchange) -> {
            try {
                String websitesJson = new String(Files.readAllBytes(Paths.get("websites.json")));
                byte[] bytes = websitesJson.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);

            } catch (IOException | RuntimeException e) {
                try {
                    String error = "{ \"error\": \"Failed to load websites\" }";
                    byte[] bytes = error.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(500, bytes.length);
                    exchange.getResponseBody().write(bytes);
                } catch (IOException ex) {
                    System.err.println("Error: " + ex.getMessage());
                }
            }
            exchange.close();
        });

        server.start();
        System.out.println("Server running at http://localhost:8000");
    }
}
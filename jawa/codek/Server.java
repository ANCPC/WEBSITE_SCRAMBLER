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
                            result = getWikipedia(topic);
                            break;
                        case "hackernews":
                            result = getHackerNews(topic);
                            break;
                        case "stackoverflow":
                            result = getStackOverflow(topic);
                            break;
                        case "github":
                            result = getGitHub(topic);
                            break;
                        case "arxiv":
                            result = getArXiv(topic);
                            break;
                        case "reddit":
                            result = getReddit(topic);
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

    // Wikipedia Fetch
    public static String getWikipedia(String topic) {
        try {
            topic = topic.replace(" ", "%20");

            String url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + topic;
            String json = readUrl(url);

            String title = extract(json, "\"title\":\"", "\",");
            String content = extract(json, "\"extract\":\"", "\",");

            if (title.equals("Not found") || content.equals("Not found")) {
                return "{ \"title\":\"No Result\", \"content\":\"Try another search.\", \"source\":\"Wikipedia\" }";
            }

            return "{ \"title\":\"" + clean(title) + "\", " +
                   "\"content\":\"" + clean(content) + "\", " +
                   "\"source\":\"Wikipedia\" }";

        } catch (Exception e) {
            return "{ \"title\":\"Wiki Error\", \"content\":\"No data\", \"source\":\"Wikipedia\" }";
        }
    }

    // HackerNews Fetch
    public static String getHackerNews(String topic) {
        try {
            String url = "https://hn.algolia.com/api/v1/search?query=" + topic;
            String json = readUrl(url);

            String title = extract(json, "\"title\":\"", "\",");

            if (title.equals("Not found")) {
                title = "No discussion found";
            }

            return "{ \"title\":\"" + clean(title) + "\", " +
                   "\"content\":\"Latest discussion related to your topic.\", " +
                   "\"source\":\"HackerNews\" }";

        } catch (Exception e) {
            return "{ \"title\":\"HN Error\", \"content\":\"No data\", \"source\":\"HackerNews\" }";
        }
    }

    // StackOverflow Fetch
    public static String getStackOverflow(String topic) {
        try {
            topic = topic.replace(" ", "%20");
            String url = "https://api.stackexchange.com/2.3/search/advanced?order=desc&sort=activity&site=stackoverflow&title=" + topic;
            String json = readUrl(url);

            String title = extract(json, "\"title\":\"", "\",");
            
            if (title.equals("Not found")) {
                return "{ \"title\":\"No Result\", \"content\":\"No Stack Overflow discussions found.\", \"source\":\"Stack Overflow\" }";
            }

            return "{ \"title\":\"" + clean(title) + "\", " +
                   "\"content\":\"Check Stack Overflow for solutions and discussions.\", " +
                   "\"source\":\"Stack Overflow\" }";

        } catch (Exception e) {
            return "{ \"title\":\"SO Error\", \"content\":\"No data\", \"source\":\"Stack Overflow\" }";
        }
    }

    // GitHub Fetch
    public static String getGitHub(String topic) {
        try {
            topic = topic.replace(" ", "%20");
            String url = "https://api.github.com/search/repositories?q=" + topic + "&sort=stars&order=desc";
            String json = readUrl(url);

            String name = extract(json, "\"name\":\"", "\",");
            String description = extract(json, "\"description\":\"", "\",");
            
            if (name.equals("Not found")) {
                return "{ \"title\":\"No Result\", \"content\":\"No GitHub repositories found.\", \"source\":\"GitHub\" }";
            }

            return "{ \"title\":\"" + clean(name) + "\", " +
                   "\"content\":\"" + clean(description.isEmpty() ? "Check GitHub for code." : description) + "\", " +
                   "\"source\":\"GitHub\" }";

        } catch (Exception e) {
            return "{ \"title\":\"GitHub Error\", \"content\":\"No data\", \"source\":\"GitHub\" }";
        }
    }

    // ArXiv Fetch
    public static String getArXiv(String topic) {
        try {
            topic = topic.replace(" ", "%20");
            String url = "https://export.arxiv.org/api/query?search_query=all:" + topic + "&max_results=1";
            String json = readUrl(url);

            String title = extract(json, "<title>", "</title>");
            
            if (title.equals("Not found") || title.isEmpty()) {
                return "{ \"title\":\"No Result\", \"content\":\"No ArXiv papers found.\", \"source\":\"ArXiv\" }";
            }

            return "{ \"title\":\"" + clean(title) + "\", " +
                   "\"content\":\"Academic paper from ArXiv. Check the source for full details.\", " +
                   "\"source\":\"ArXiv\" }";

        } catch (Exception e) {
            return "{ \"title\":\"ArXiv Error\", \"content\":\"No data\", \"source\":\"ArXiv\" }";
        }
    }

    // Reddit Fetch
    public static String getReddit(String topic) {
        try {
            topic = topic.replace(" ", "%20");
            String url = "https://www.reddit.com/search.json?q=" + topic + "&sort=relevance";
            String json = readUrl(url);

            String title = extract(json, "\"title\":\"", "\",");
            
            if (title.equals("Not found")) {
                return "{ \"title\":\"No Result\", \"content\":\"No Reddit posts found.\", \"source\":\"Reddit\" }";
            }

            return "{ \"title\":\"" + clean(title) + "\", " +
                   "\"content\":\"Community discussion on Reddit. Check for various perspectives.\", " +
                   "\"source\":\"Reddit\" }";

        } catch (Exception e) {
            return "{ \"title\":\"Reddit Error\", \"content\":\"No data\", \"source\":\"Reddit\" }";
        }
    }

    // Fetch URL content
    public static String readUrl(String urlString) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(urlString).openStream())
        )) {
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];

            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }

            return buffer.toString();
        }
    }

    // Extract JSON value safely
    public static String extract(String text, String start, String end) {
        try {
            int i = text.indexOf(start);
            if (i == -1) return "Not found";
            i += start.length();
            int j = text.indexOf(end, i);
            return text.substring(i, j);
        } catch (Exception e) {
            return "Error";
        }
    }

    // Clean text
    public static String clean(String s) {
        return s.replace("\"", "'").replace("\\n", " ");
    }
}
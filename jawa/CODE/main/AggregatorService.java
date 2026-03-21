package com.example.demo.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class AggregatorService {

    public List<Map<String, String>> fetchData(String topic) {
        List<Map<String, String>> results = new ArrayList<>();

        try {
            // Wikipedia API
            String wikiUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/" + topic;
            JSONObject wikiJson = new JSONObject(readUrl(wikiUrl));

            Map<String, String> wikiData = new HashMap<>();
            wikiData.put("title", wikiJson.optString("title"));
            wikiData.put("content", wikiJson.optString("extract"));
            wikiData.put("source", "Wikipedia");

            results.add(wikiData);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Hacker News API
            String hnUrl = "https://hn.algolia.com/api/v1/search?query=" + topic;
            JSONObject hnJson = new JSONObject(readUrl(hnUrl));
            JSONArray hits = hnJson.getJSONArray("hits");

            for (int i = 0; i < Math.min(3, hits.length()); i++) {
                JSONObject obj = hits.getJSONObject(i);

                Map<String, String> hnData = new HashMap<>();
                hnData.put("title", obj.optString("title"));
                hnData.put("content", obj.optString("story_text", "No content"));
                hnData.put("source", "HackerNews");

                results.add(hnData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private String readUrl(String urlString) throws Exception {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(urlString).openStream())
        );
        StringBuilder buffer = new StringBuilder();
        int read;
        char[] chars = new char[1024];
        while ((read = reader.read(chars)) != -1)
            buffer.append(chars, 0, read);

        return buffer.toString();
    }
}
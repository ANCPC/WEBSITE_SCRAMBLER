package com.example.demo.controller;

import com.example.demo.service.AggregatorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class SearchController {

    private final AggregatorService service;

    public SearchController(AggregatorService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public List<Map<String, String>> search(@RequestParam String topic) {
        return service.fetchData(topic);
    }
}

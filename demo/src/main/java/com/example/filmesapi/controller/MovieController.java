package com.example.filmesapi.controller;

import com.example.filmesapi.model.Movie;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final RestTemplate restTemplate;
    private final Map<String, Movie> favoriteMovies = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper(); // Adicionando o ObjectMapper

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    public MovieController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/add")
    public String addMovie(@RequestBody Movie movie) {
        if (movie.getId() == null || movie.getId().isEmpty()) {
            return "Movie ID is required";
        }
        favoriteMovies.put(movie.getId(), movie);
        return "Movie added to favorites";
    }

    @GetMapping("/get/{id}")
    public Movie getMovie(@PathVariable String id) {
        return favoriteMovies.get(id);
    }

    @GetMapping("/all")
    public Collection<Movie> getAllMovies() {
        return favoriteMovies.values();
    }

    @GetMapping("/search/{query}")
    public List<Map<String, String>> searchMovies(@PathVariable String query) {
        String url = String.format("https://api.themoviedb.org/3/search/movie?api_key=%s&query=%s", tmdbApiKey, query);
        String response = restTemplate.getForObject(url, String.class);

        List<Map<String, String>> moviesList = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");

            for (JsonNode node : results) {
                Map<String, String> movieData = new HashMap<>();
                movieData.put("title", node.path("title").asText());
                movieData.put("description", node.path("overview").asText());
                movieData.put("release_date", node.path("release_date").asText());
                moviesList.add(movieData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return moviesList;
    }
}

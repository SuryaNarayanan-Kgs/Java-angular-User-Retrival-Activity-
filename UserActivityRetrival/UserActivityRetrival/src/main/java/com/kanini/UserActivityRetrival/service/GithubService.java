package com.kanini.UserActivityRetrival.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class GithubService {
    private static final String GITHUB_API = "https://api.github.com";

    @Autowired
    private RestTemplate restTemplate;

    private HttpHeaders buildHeaders(String token) {
        HttpHeaders h = new HttpHeaders();
        h.set("Accept", "application/vnd.github+json");
        h.set("X-GitHub-Api-Version", "2022-11-28");
        if (token != null && !token.isBlank()) {
            h.set("Authorization", "Bearer " + token);
        }
        return h;
    }

    public ResponseEntity<String> validateToken(String token) {
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(token));
        try {
            return restTemplate.exchange(GITHUB_API + "/user", HttpMethod.GET, entity, String.class);
        } catch (RestClientException ex) {
            // convert errors into proper response entity with status 401 or 500
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"invalid token or request failed\"}");
        }
    }

    public ResponseEntity<String> getOrgRepos(String org, String token) {
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(token));
        String url = GITHUB_API + "/orgs/" + org + "/repos";
        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (RestClientException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("{\"error\":\"github request failed\"}");
        }
    }

    public ResponseEntity<String> getCommits(String owner, String repo, Map<String, String> queryParams, String token) {
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(token));
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GITHUB_API + "/repos/" + owner + "/" + repo + "/commits");
        if (queryParams != null) {
            queryParams.forEach(builder::queryParam);
        }
        try {
            return restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
        } catch (RestClientException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("{\"error\":\"github request failed\"}");
        }
    }
}
package com.kanini.UserActivityRetrival.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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
        HttpHeaders headers = buildHeaders(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String authorsParam = queryParams != null ? queryParams.get("author") : null;
        String[] authors = (authorsParam != null && !authorsParam.isBlank()) ? authorsParam.split(",") : new String[]{null};

        StringBuilder allCommitsJson = new StringBuilder();
        allCommitsJson.append("[");
        boolean firstOverall = true;

        for (String author : authors) {
            int page = 1;
            int perPage = 100;
            while (true) {
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GITHUB_API + "/repos/" + owner + "/" + repo + "/commits").queryParam("page", page).queryParam("per_page", perPage);

                if (queryParams != null) {
                    queryParams.forEach((k, v) -> {
                        if (!k.equals("author")) {
                            builder.queryParam(k, v);
                        }
                    });
                }
                if (author != null && !author.isBlank()) {
                    builder.queryParam("author", author.trim());
                }
                ResponseEntity<String> response;
                try {
                    response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
                } catch (RestClientException ex) {
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("{\"error\":\"github request failed\"}");
                }
                String body = response.getBody();
                if (body == null || body.equals("[]")) {
                    break;
                }
                if (!firstOverall) {
                    allCommitsJson.append(",");
                }
                allCommitsJson.append(body, 1, body.length() - 1);
                firstOverall = false;
                if (body.split("\\{").length - 1 < perPage) {
                    break;
                }
                page++;
            }
        }
        allCommitsJson.append("]");
        return ResponseEntity.ok(allCommitsJson.toString());
    }
}
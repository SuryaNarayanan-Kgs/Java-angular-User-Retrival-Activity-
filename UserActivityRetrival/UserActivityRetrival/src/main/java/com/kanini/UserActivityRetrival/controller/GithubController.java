package com.kanini.UserActivityRetrival.controller;

import com.kanini.UserActivityRetrival.service.GithubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(
        name = "User git details"
)
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class GithubController {

    @Autowired
    private GithubService githubService;

    @Operation(
            summary = "",
            description = "validating the user authentication"
    )
    @ApiResponse(
            responseCode = "201",
            description = "HTTP Status 201 CREATED"
    )
    @GetMapping("/auth/validate")
    public ResponseEntity<String> validateToken(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"Authorization header missing\"}");
        }
        return githubService.validateToken(token);
    }

    @Operation(
            summary = "",
            description = "Get oraganization Repository"
    )
    @ApiResponse(
            responseCode = "201",
            description = "HTTP Status 201 CREATED"
    )
    @GetMapping("/{org}/repos")
    public ResponseEntity<String> getOrgRepos(
            @PathVariable String org,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        return githubService.getOrgRepos(org, token);
    }

    @Operation(
            summary = "",
            description = "Get user commits"
    )
    @ApiResponse(
            responseCode = "201",
            description = "HTTP Status 201 CREATED"
    )
    @GetMapping("/repos/{owner}/{repo}/commits")
    public ResponseEntity<String> getCommits(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(required = false) Map<String, String> queryParams,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        return githubService.getCommits(owner, repo, queryParams, token);
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null) return null;
        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
    }
}
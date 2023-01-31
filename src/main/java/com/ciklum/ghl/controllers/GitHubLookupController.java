package com.ciklum.ghl.controllers;

import com.ciklum.ghl.dto.DtoConverter;
import com.ciklum.ghl.dto.RepositoryDto;
import com.ciklum.ghl.services.github.GitHubCache;
import com.ciklum.ghl.services.github.GitHubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class GitHubLookupController {

    private final GitHubService gitHubService;

    private final GitHubCache cache;

    public GitHubLookupController(GitHubService gitHubService, GitHubCache cache) {
        this.gitHubService = gitHubService;
        this.cache = cache;
    }

    @GetMapping(value = "/users/{user}")
    public ResponseEntity<List<RepositoryDto>> getUser(@PathVariable(value = "user") String user) {
        List<RepositoryDto> result = cache.get(user);
        if (result == null) {
            result = gitHubService
                    .getRepositories(user)
                    .stream()
                    .map(DtoConverter::convert)
                    .toList();
            cache.set(user, result);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

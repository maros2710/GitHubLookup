package com.ciklum.ghl.services.github;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class GitHubService {

    private final GitHubClient client;

    private final GitHubCache cache;

    public GitHubService(GitHubClient client, GitHubCache cache) {
        this.client = client;
        this.cache = cache;
    }

    public List<GitHubRepository> getRepositories(String user) {
        log.info(String.format("Looking up for repositories for %s", user));

        List<GitHubRepository> repositories = cache.get(user);
        if (repositories != null) {
            log.debug("Returning from cache");
            return repositories;
        }

        repositories = client.getRepositories(user);

        //filter out forked
        repositories = repositories
                .stream()
                .filter(repo -> repo.getFork() == null || !repo.getFork())
                .toList();

        cache.set(user, repositories);

        return repositories;
    }
}

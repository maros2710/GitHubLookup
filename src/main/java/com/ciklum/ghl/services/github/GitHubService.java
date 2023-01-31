package com.ciklum.ghl.services.github;

import com.ciklum.ghl.dto.DtoConverter;
import com.ciklum.ghl.dto.RepositoryDto;
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

    public List<RepositoryDto> getRepositories(String user) {
        log.info(String.format("Looking up for repositories for %s", user));

        List<RepositoryDto> result = cache.get(user);
        if (result != null) {
            log.debug("Returning from cache");
            return result;
        }

        List<GitHubRepository> repositories = client.getRepositories(user);

        //filter out forked
        repositories = repositories
                .stream()
                .filter(repo -> repo.getFork() == null || !repo.getFork())
                .toList();

        result = repositories.stream()
                             .map(DtoConverter::convert)
                             .toList();

        cache.set(user, result);

        return result;
    }
}

package com.ciklum.ghl.services.github;

import com.ciklum.ghl.dto.RepositoryDto;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caches all the requests for configurable timeout in application.properties
 */
@Service
public class GitHubCache {

    private final Map<String, List<RepositoryDto>> userCache;

    private final Map<String, Long> timestampCache;

    private final GitHubConfiguration gitHubConfiguration;

    public GitHubCache(GitHubConfiguration gitHubConfiguration) {
        this.userCache = new HashMap<>();
        this.timestampCache = new HashMap<>();
        this.gitHubConfiguration = gitHubConfiguration;
    }

    public void set(String user, List<RepositoryDto> repositories) {
        userCache.put(user, repositories);
        timestampCache.put(user, ZonedDateTime.now().toInstant().toEpochMilli());
    }

    public List<RepositoryDto> get(String user) {
        Long lastTime = timestampCache.get(user);
        if (lastTime != null) {
            Long currentTime = ZonedDateTime.now().toInstant().toEpochMilli();
            if (currentTime - lastTime >= gitHubConfiguration.getCacheTimeout()) {
                userCache.remove(user);
                timestampCache.remove(user);
                return null;
            }
            return userCache.get(user);
        }
        return null;
    }
}

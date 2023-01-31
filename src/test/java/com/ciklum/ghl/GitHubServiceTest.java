package com.ciklum.ghl;

import com.ciklum.ghl.dto.RepositoryDto;
import com.ciklum.ghl.services.github.GitHubCache;
import com.ciklum.ghl.services.github.GitHubClient;
import com.ciklum.ghl.services.github.GitHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.ciklum.ghl.TestData.gitHubRepository;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class GitHubServiceTest {

    @InjectMocks
    private GitHubService gitHubService;

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private GitHubCache gitHubCache;

    @BeforeEach
    public void init() {
        when(gitHubCache.get(any())).thenReturn(null);
    }

    @Test
    public void testGetRepositories() {
        when(gitHubClient.getRepositories(any())).thenReturn(List.of(gitHubRepository(false)));

        List<RepositoryDto> repos = gitHubService.getRepositories("user");

        assertNotNull(repos);
        assertTrue(repos.size() == 1);
        assertTrue(repos.get(0).getRepositoryName().equals("Repository"));
        assertTrue(repos.get(0).getBranches().size() == 1);
        assertTrue(repos.get(0).getBranches().get(0).getName().equals("master"));
    }

    @Test
    public void testFilterForkedRepositories() {
        when(gitHubClient.getRepositories(any())).thenReturn(List.of(gitHubRepository(false), gitHubRepository(true)));

        List<RepositoryDto> repos = gitHubService.getRepositories("user");

        assertNotNull(repos);
        assertTrue(repos.size() == 1);
        assertTrue(repos.get(0).getRepositoryName().equals("Repository"));
        assertTrue(repos.get(0).getBranches().size() == 1);
        assertTrue(repos.get(0).getBranches().get(0).getName().equals("master"));
    }
}

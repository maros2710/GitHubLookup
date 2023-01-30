package com.ciklum.ghl;

import com.ciklum.ghl.config.GitHubConfiguration;
import com.ciklum.ghl.services.github.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;

@SpringBootTest
public class GitHubServiceTest {

    @InjectMocks
    private GitHubService gitHubService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GitHubConfiguration gitHubConfiguration;

    @Autowired
    private TaskExecutor executor;

    @BeforeEach
    public void init() {
        try {
            Field field = GitHubService.class.getDeclaredField("executor");
            field.setAccessible(true);
            field.set(gitHubService, executor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        GitHubUser user = new GitHubUser();
        user.setName("Marian Hacaj");
        user.setLogin("maros2710");
        user.setPublicRepos(2);

        when(restTemplate.getForObject(any(String.class), any())).thenReturn(user);

        GitHubBranch branch = new GitHubBranch();
        branch.setName("master");
        branch.setCommit(new GitHubCommit());

        GitHubRepository repository = new GitHubRepository();
        repository.setName("Repository");
        repository.setFork(false);
        repository.setBranchesCount(1);
        repository.setBranchesUrl("branches?per_page");
        GitHubOwner owner = new GitHubOwner();
        owner.setLogin("login");
        repository.setOwner(owner);

        ResponseEntity<List<GitHubRepository>> repositories =
                new ResponseEntity<>(List.of(repository), HttpStatusCode.valueOf(HttpStatus.OK.value()));
        when(restTemplate.exchange(contains("repos?per_page"), any(), any(),
                any(ParameterizedTypeReference.class))).thenReturn(repositories);

        ResponseEntity<List<GitHubBranch>> branches =
                new ResponseEntity<>(List.of(branch), HttpStatusCode.valueOf(HttpStatus.OK.value()));
        when(restTemplate.exchange(contains("branches?per_page"), any(), any(),
                any(ParameterizedTypeReference.class))).thenReturn(branches);

        when(gitHubConfiguration.getToken()).thenReturn("");
    }

    @Test
    public void testGetRepositories() {
        List<GitHubRepository> repos = gitHubService.getRepositories("maros2710");
        assertNotNull(repos);
        assertTrue(repos.size() == 1);
        assertTrue(repos.get(0).getName().equals("Repository"));
        assertTrue(repos.get(0).getBranches().size() == 1);
        assertTrue(repos.get(0).getBranches().get(0).getName().equals("master"));

    }
}

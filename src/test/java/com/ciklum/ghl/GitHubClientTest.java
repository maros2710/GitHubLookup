package com.ciklum.ghl;

import com.ciklum.ghl.config.GitHubConfiguration;
import com.ciklum.ghl.services.github.GitHubClient;
import com.ciklum.ghl.services.github.GitHubRepository;
import com.ciklum.ghl.services.github.dto.GitHubBranch;
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

import static com.ciklum.ghl.TestData.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;

@SpringBootTest
public class GitHubClientTest {

    @InjectMocks
    private GitHubClient client;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GitHubConfiguration gitHubConfiguration;

    @Autowired
    private TaskExecutor executor;

    @BeforeEach
    public void init() {
        try {
            Field field = GitHubClient.class.getDeclaredField("executor");
            field.setAccessible(true);
            field.set(client, executor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        when(restTemplate.getForObject(any(String.class), any())).thenReturn(gitHubUser());

        ResponseEntity<List<GitHubRepository>> repositories =
                new ResponseEntity<>(List.of(gitHubRepository(false)), HttpStatusCode.valueOf(HttpStatus.OK.value()));
        when(restTemplate.exchange(contains("repos?per_page"), any(), any(),
                any(ParameterizedTypeReference.class))).thenReturn(repositories);

        ResponseEntity<List<GitHubBranch>> branches =
                new ResponseEntity<>(List.of(gitHubBranch()), HttpStatusCode.valueOf(HttpStatus.OK.value()));
        when(restTemplate.exchange(contains("branches?per_page"), any(), any(),
                any(ParameterizedTypeReference.class))).thenReturn(branches);

        when(gitHubConfiguration.getToken()).thenReturn("");
    }

    @Test
    public void testGetRepositories() {
        List<GitHubRepository> repos = client.getRepositories("maros2710");
        assertNotNull(repos);
        assertTrue(repos.size() == 1);
        assertTrue(repos.get(0).getName().equals("Repository"));
        assertTrue(repos.get(0).getBranches().size() == 1);
        assertTrue(repos.get(0).getBranches().get(0).getName().equals("master"));

    }
}

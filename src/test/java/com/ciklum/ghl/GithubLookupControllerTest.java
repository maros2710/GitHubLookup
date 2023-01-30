package com.ciklum.ghl;

import com.ciklum.ghl.services.github.GitHubCache;
import com.ciklum.ghl.services.github.GitHubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GitHubLookupController.class)
public class GithubLookupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubService gitHubService;

    @MockBean
    private GitHubCache githubCache;

    @MockBean
    private RestTemplateBuilder restTemplateBuilder;

    @Test
    public void givenExistingUser_thenStatus200() throws Exception {
        when(gitHubService.getRepositories(any())).thenReturn(new ArrayList<>());
        when(githubCache.get(any())).thenReturn(null);

        this.mockMvc.perform(get("/users/user_name")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void givenNonExistingUser_thenStatus404() throws Exception {
        when(gitHubService.getRepositories(any())).thenThrow(
                new HttpClientErrorException(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value())));
        when(githubCache.get(any())).thenReturn(null);

        this.mockMvc.perform(get("/users/non_existing_user")).andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    public void givenBadAcceptHeader_thenStatus406() throws Exception {
        when(gitHubService.getRepositories(any())).thenThrow(
                new HttpClientErrorException(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value())));
        when(githubCache.get(any())).thenReturn(null);

        this.mockMvc.perform(get("/users/user_name").header("Accept", "application/xml"))
                    .andDo(print())
                    .andExpect(status().isNotAcceptable());
    }
}

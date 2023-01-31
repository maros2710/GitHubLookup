package com.ciklum.ghl.services.github;

import com.ciklum.ghl.config.GitHubConfiguration;
import com.ciklum.ghl.services.github.dto.GitHubBranch;
import com.ciklum.ghl.services.github.dto.GitHubUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class GitHubClient {

    private final RestTemplate restTemplate;

    private final Executor executor;

    public GitHubClient(RestTemplate restTemplate, GitHubConfiguration configuration, @Qualifier("githubExecutor") Executor executor) {
        this.restTemplate = restTemplate;
        this.restTemplate.setInterceptors(List.of(new AuthorizationInterceptor(configuration.getToken())));
        this.executor = executor;
    }

    public List<GitHubRepository> getRepositories(String user) {
        log.info(String.format("Looking up for repositories for %s", user));

        //First, we find GitHub user
        GitHubUser gitHubUser = findUser(user);

        //Second, we find all users repositories
        List<GitHubRepository> repos = getRepos(gitHubUser);

        //In GitHub, there is no API to get branches count in repository,
        //but we can init branch count from response in Link header
        initBranchesCount(repos);

        //Finally, we find branches for the user
        repos.forEach(it -> it.setBranches(getBranches(it)));

        return repos;
    }


    private GitHubUser findUser(String user) {
        log.info(String.format("Looking up for %s", user));
        String url = String.format("https://api.github.com/users/%s", user);
        GitHubUser gitHubUser = restTemplate.getForObject(url, GitHubUser.class);
        return gitHubUser;
    }

    private List<GitHubRepository> getRepos(GitHubUser user) {
        //User can have many repositories, so we acquire them with paging
        //GitHub API allows up to 100 records per page
        int pages = user.getPublicRepos() / 100 + 1;

        List<CompletableFuture<List<GitHubRepository>>> features = new ArrayList<>();

        //we can do all pages at once asynchronously
        for (int i = 0; i < pages; i++) {
            features.add(findRepos(user.getLogin(), i + 1));
        }

        CompletableFuture.allOf(features.toArray(new CompletableFuture[0])).join();

        List<GitHubRepository> result = new ArrayList<>();
        features.forEach(it -> {
            try {
                result.addAll(it.get());
            } catch (Exception ex) {
                log.error("Could not get repos", ex);
            }
        });

        return result;
    }


    private CompletableFuture<List<GitHubRepository>> findRepos(String user, int page) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug(String.format("Looking for repositories for user %s and page %s", user, page));
            String url = String.format("https://api.github.com/users/%s/repos?per_page=100&page=%s", user, page);
            ResponseEntity<List<GitHubRepository>> res =
                    restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                    });
            List<GitHubRepository> repos = res.getBody();
            return repos;
        }, executor);
    }

    private List<GitHubBranch> getBranches(GitHubRepository repo) {
        int pages = repo.getBranchesCount() / 100 + 1;

        List<CompletableFuture<List<GitHubBranch>>> features = new ArrayList<>();

        for (int i = 0; i < pages; i++) {
            features.add(findBranches(repo, i + 1));
        }

        CompletableFuture.allOf(features.toArray(new CompletableFuture[0])).join();

        List<GitHubBranch> result = new ArrayList<>();
        features.forEach(it -> {
            try {
                result.addAll(it.get());
            } catch (Exception ex) {
                log.error("Could not get repos", ex);
            }
        });

        return result;
    }

    private CompletableFuture<List<GitHubBranch>> findBranches(GitHubRepository repo, int page) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug(String.format("Looking for branches for repo %s and page %s", repo.getName(), page));
            String url = String.format(repo.getBranchesUrl().replace("{/branch}", "") + "?per_page=100&page=%s", page);

            ResponseEntity<List<GitHubBranch>> res =
                    restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                    });
            List<GitHubBranch> branches = res.getBody();
            return branches;
        }, executor);
    }

    private void initBranchesCount(List<GitHubRepository> repos) {
        List<CompletableFuture<Boolean>> features = new ArrayList<>();

        repos.forEach(it -> features.add(getBranchesCount(it)));

        CompletableFuture.allOf(features.toArray(new CompletableFuture[0])).join();
    }

    //This is a trick to get branches count, so we can request for all pages at once
    //When there is more than one branch, and we ask GitHUb to get one branch per page,
    //GitHub returns response with Link header that contains links to pages:
    //<https://api.github.com/repositories/29854079/branches?per_page=1&page=2>; rel="next", <https://api.github.com/repositories/29854079/branches?per_page=1&page=2>; rel="last"
    //so we can acquire branches count in "last" link from parameter "page"
    private CompletableFuture<Boolean> getBranchesCount(GitHubRepository repo) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Looking for branches count for repo " + repo.getName());
            String url = repo.getBranchesUrl().replace("{/branch}", "") + "?per_page=1";
            ResponseEntity<List<GitHubBranch>> res =
                    restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                    });
            List<String> links = res.getHeaders().get("Link");

            if (links != null && links.size() > 0) {
                String link = links.get(0);
                if (link.lastIndexOf("per_page=1&page=") > 0) {
                    link = link.substring(link.lastIndexOf("per_page=1&page=") + 16);
                    link = link.substring(0, link.indexOf(">"));
                    int count = Integer.parseInt(link.trim());
                    repo.setBranchesCount(count);
                    return true;
                }
            }

            repo.setBranchesCount(Objects.requireNonNull(res.getBody()).size());
            return true;
        }, executor);
    }
}

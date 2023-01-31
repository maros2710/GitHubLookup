package com.ciklum.ghl;

import com.ciklum.ghl.services.github.GitHubRepository;
import com.ciklum.ghl.services.github.dto.GitHubBranch;
import com.ciklum.ghl.services.github.dto.GitHubCommit;
import com.ciklum.ghl.services.github.dto.GitHubOwner;
import com.ciklum.ghl.services.github.dto.GitHubUser;

import java.util.List;

public class TestData {

    public static GitHubUser gitHubUser(){
        GitHubUser user = new GitHubUser();
        user.setName("Marian Hacaj");
        user.setLogin("maros2710");
        user.setPublicRepos(2);

        return user;
    }

    public static GitHubRepository gitHubRepository(boolean fork) {
        GitHubRepository repository = new GitHubRepository();
        repository.setName("Repository");
        repository.setFork(fork);
        repository.setBranchesCount(1);
        repository.setBranchesUrl("branches?per_page");
        GitHubOwner owner = new GitHubOwner();
        owner.setLogin("login");
        repository.setOwner(owner);
        repository.setBranches(List.of(gitHubBranch()));

        return repository;
    }

    public static GitHubBranch gitHubBranch() {
        GitHubBranch branch = new GitHubBranch();
        branch.setName("master");
        branch.setCommit(new GitHubCommit());

        return branch;
    }
}
